package de.interwebmedia.report.common.service;

import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import de.interwebmedia.report.common.mongo.PendingNotificationModel;
import dev.morphia.Datastore;
import dev.morphia.query.experimental.filters.Filters;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;

@RequiredArgsConstructor
public class PendingNotificationService {

    private final Datastore datastore;
    private final PlatformAsyncExecutor asyncExecutor;

    public CompletableFuture<Void> store(UUID recipient, String message) {
        return asyncExecutor.runAsync(() -> datastore.save(PendingNotificationModel.builder()
                .id(new ObjectId())
                .recipient(recipient)
                .message(message)
                .createdAt(Instant.now())
                .build()));
    }

    public CompletableFuture<List<PendingNotificationModel>> pull(UUID recipient) {
        return asyncExecutor.supplyAsync(() -> {
            List<PendingNotificationModel> entries = datastore.find(PendingNotificationModel.class)
                    .filter(Filters.eq("recipient", recipient))
                    .iterator().toList();
            entries.forEach(entry -> datastore.find(PendingNotificationModel.class)
                    .filter(Filters.eq("_id", entry.getId()))
                    .delete());
            return entries;
        });
    }
}
