package de.interwebmedia.report.common.service;

import de.interwebmedia.report.api.dto.ReportCreateRequest;
import de.interwebmedia.report.api.model.ReportStatus;
import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import de.interwebmedia.report.api.service.ReportService;
import de.interwebmedia.report.common.messaging.RedisChannels;
import de.interwebmedia.report.common.messaging.RedisPublisher;
import de.interwebmedia.report.common.mongo.ReportModel;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final Datastore datastore;
    private final RedisPublisher redisPublisher;
    private final PlatformAsyncExecutor asyncExecutor;

    @Override
    public CompletableFuture<String> createReport(ReportCreateRequest request) {
        return asyncExecutor.supplyAsync(() -> {
            ReportModel model = ReportModel.builder()
                    .id(new ObjectId())
                    .reporter(request.reporterUuid())
                    .reported(request.reportedUuid())
                    .reporterName(request.reporterName())
                    .reportedName(request.reportedName())
                    .reason(request.reasonToStore())
                    .status(ReportStatus.OPEN)
                    .server(request.server())
                    .createdAt(Instant.now())
                    .build();
            datastore.save(model);

            String payload = """
                    {"reportId":"%s","reporter":"%s","reported":"%s","reason":"%s","server":"%s","timestamp":"%s"}
                    """.formatted(
                    model.getId().toHexString(),
                    model.getReporterName(),
                    model.getReportedName(),
                    model.getReason(),
                    model.getServer(),
                    model.getCreatedAt()
            );
            redisPublisher.publish(RedisChannels.NEW_REPORT, payload);
            return model.getId().toHexString();
        });
    }

    @Override
    public CompletableFuture<Void> changeStatus(String reportId, ReportStatus status, String moderatorName, String moderatorNote) {
        return asyncExecutor.runAsync(() -> {
            ReportModel report = datastore.find(ReportModel.class)
                    .filter(Filters.eq("_id", new ObjectId(reportId)))
                    .first();
            if (report == null) {
                throw new IllegalArgumentException("Unknown report id: " + reportId);
            }

            report.setStatus(status);
            report.setHandledByName(moderatorName);
            report.setHandledBy(UUID.nameUUIDFromBytes(moderatorName.getBytes()));
            report.setModNote(moderatorNote);
            report.setHandledAt(Instant.now());
            datastore.save(report);

            String payload = """
                    {"reportId":"%s","reporterUuid":"%s","reporterName":"%s","reportedName":"%s","status":"%s","handledBy":"%s","timestamp":"%s"}
                    """.formatted(
                    report.getId().toHexString(),
                    report.getReporter(),
                    report.getReporterName(),
                    report.getReportedName(),
                    report.getStatus(),
                    report.getHandledByName(),
                    report.getHandledAt()
            );
            redisPublisher.publish(RedisChannels.STATUS_UPDATE, payload);
        });
    }

    @Override
    public CompletableFuture<Long> openReportCount() {
        return asyncExecutor.supplyAsync(() -> datastore.find(ReportModel.class)
                .filter(Filters.eq("status", ReportStatus.OPEN))
                .count());
    }
}
