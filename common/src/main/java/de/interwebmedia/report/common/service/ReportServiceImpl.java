package de.interwebmedia.report.common.service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.interwebmedia.report.api.dto.ModerationActionRequest;
import de.interwebmedia.report.api.dto.ReportCreateRequest;
import de.interwebmedia.report.api.dto.ReportFilter;
import de.interwebmedia.report.api.dto.ReportStats;
import de.interwebmedia.report.api.dto.ReportView;
import de.interwebmedia.report.api.model.ReportStatus;
import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import de.interwebmedia.report.api.service.ReportService;
import de.interwebmedia.report.common.messaging.RedisChannels;
import de.interwebmedia.report.common.messaging.RedisPublisher;
import de.interwebmedia.report.common.mongo.ReportModel;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.experimental.filters.Filters;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final Datastore datastore;
    private final RedisPublisher redisPublisher;
    private final PlatformAsyncExecutor asyncExecutor;
    private final Gson gson = new Gson();

    @Override
    public CompletableFuture<String> createReport(ReportCreateRequest request) {
        return asyncExecutor.supplyAsync(() -> {
            validateCreateRequest(request);

            ReportModel model = ReportModel.builder()
                    .id(new ObjectId())
                    .reporter(request.reporterUuid())
                    .reported(request.reportedUuid())
                    .reporterName(request.reporterName())
                    .reportedName(request.reportedName())
                    .reason(request.reasonToStore())
                    .template(request.template().name())
                    .status(ReportStatus.OPEN)
                    .server(request.server())
                    .createdAt(Instant.now())
                    .build();
            datastore.save(model);

            JsonObject payload = new JsonObject();
            payload.addProperty("reportId", model.getId().toHexString());
            payload.addProperty("reporter", model.getReporterName());
            payload.addProperty("reported", model.getReportedName());
            payload.addProperty("reason", model.getReason());
            payload.addProperty("server", model.getServer());
            payload.addProperty("timestamp", model.getCreatedAt().toString());
            redisPublisher.publish(RedisChannels.NEW_REPORT, gson.toJson(payload));
            return model.getId().toHexString();
        });
    }

    @Override
    public CompletableFuture<Void> moderate(ModerationActionRequest request) {
        return asyncExecutor.runAsync(() -> {
            if (request.targetStatus() == ReportStatus.OPEN) {
                throw new IllegalArgumentException("Moderation target status must be RESOLVED or REJECTED");
            }

            ReportModel report = datastore.find(ReportModel.class)
                    .filter(Filters.eq("_id", new ObjectId(request.reportId())))
                    .first();
            if (report == null) {
                throw new IllegalArgumentException("Unknown report id: " + request.reportId());
            }
            if (report.getStatus() != ReportStatus.OPEN) {
                throw new IllegalStateException("Report is already handled: " + report.getStatus());
            }

            report.setStatus(request.targetStatus());
            report.setHandledByName(request.moderatorName());
            report.setHandledBy(request.moderatorUuid() != null
                    ? request.moderatorUuid()
                    : UUID.nameUUIDFromBytes(request.moderatorName().getBytes(StandardCharsets.UTF_8)));
            report.setModNote(request.moderatorNote());
            report.setHandledAt(Instant.now());
            datastore.save(report);

            JsonObject payload = new JsonObject();
            payload.addProperty("reportId", report.getId().toHexString());
            payload.addProperty("reporterUuid", String.valueOf(report.getReporter()));
            payload.addProperty("reporterName", report.getReporterName());
            payload.addProperty("reportedName", report.getReportedName());
            payload.addProperty("status", report.getStatus().name());
            payload.addProperty("handledBy", report.getHandledByName());
            payload.addProperty("modNote", report.getModNote());
            payload.addProperty("timestamp", report.getHandledAt().toString());
            redisPublisher.publish(RedisChannels.STATUS_UPDATE, gson.toJson(payload));
        });
    }

    @Override
    public CompletableFuture<Optional<ReportView>> details(String reportId) {
        return asyncExecutor.supplyAsync(() -> Optional.ofNullable(datastore.find(ReportModel.class)
                        .filter(Filters.eq("_id", new ObjectId(reportId)))
                        .first())
                .map(this::toView));
    }

    @Override
    public CompletableFuture<List<ReportView>> list(ReportFilter filter) {
        return asyncExecutor.supplyAsync(() -> {
            Query<ReportModel> query = datastore.find(ReportModel.class);
            if (!filter.statuses().isEmpty()) {
                query.filter(Filters.in("status", filter.statuses()));
            }
            if (!filter.servers().isEmpty()) {
                query.filter(Filters.in("server", filter.servers()));
            }
            if (!filter.templates().isEmpty()) {
                query.filter(Filters.in("template", filter.templates()));
            }
            if (filter.createdAfter() != null) {
                query.filter(Filters.gte("createdAt", filter.createdAfter()));
            }
            if (filter.createdBefore() != null) {
                query.filter(Filters.lte("createdAt", filter.createdBefore()));
            }

            FindOptions options = new FindOptions().sort(dev.morphia.query.Sort.descending("createdAt"));
            if (filter.limit() > 0) {
                options.limit(filter.limit());
            }

            return query.iterator(options).toList().stream().map(this::toView).collect(Collectors.toList());
        });
    }

    @Override
    public CompletableFuture<ReportStats> stats(String playerName) {
        return asyncExecutor.supplyAsync(() -> {
            long total = datastore.find(ReportModel.class)
                    .filter(Filters.eq("reportedName", playerName))
                    .count();
            long open = datastore.find(ReportModel.class)
                    .filter(Filters.eq("reportedName", playerName), Filters.eq("status", ReportStatus.OPEN))
                    .count();
            long resolved = datastore.find(ReportModel.class)
                    .filter(Filters.eq("reportedName", playerName), Filters.eq("status", ReportStatus.RESOLVED))
                    .count();
            long rejected = datastore.find(ReportModel.class)
                    .filter(Filters.eq("reportedName", playerName), Filters.eq("status", ReportStatus.REJECTED))
                    .count();
            return new ReportStats(playerName, total, open, resolved, rejected);
        });
    }

    @Override
    public CompletableFuture<Long> openReportCount() {
        return asyncExecutor.supplyAsync(() -> datastore.find(ReportModel.class)
                .filter(Filters.eq("status", ReportStatus.OPEN))
                .count());
    }

    private void validateCreateRequest(ReportCreateRequest request) {
        if (request.reporterUuid() == null || request.reportedUuid() == null) {
            throw new IllegalArgumentException("Reporter and reported UUID are required");
        }
        if (request.reporterName() == null || request.reporterName().isBlank()) {
            throw new IllegalArgumentException("Reporter name is required");
        }
        if (request.reportedName() == null || request.reportedName().isBlank()) {
            throw new IllegalArgumentException("Reported name is required");
        }
        if (request.template().requiresCustomText() && (request.customReason() == null || request.customReason().isBlank())) {
            throw new IllegalArgumentException("Custom reason is required for OTHER template");
        }
    }

    private ReportView toView(ReportModel model) {
        return new ReportView(
                model.getId().toHexString(),
                model.getReporter(),
                model.getReported(),
                model.getReporterName(),
                model.getReportedName(),
                model.getReason(),
                model.getStatus(),
                model.getHandledBy(),
                model.getHandledByName(),
                model.getModNote(),
                model.getServer(),
                model.getCreatedAt(),
                model.getHandledAt()
        );
    }
}
