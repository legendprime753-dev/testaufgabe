package de.interwebmedia.report.common.mongo;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.Indexes;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Field;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity("pending_notifications")
@Indexes({
        @Index(fields = @Field("recipient"), options = @IndexOptions(name = "idx_recipient")),
        @Index(fields = @Field("createdAt"), options = @IndexOptions(name = "idx_pending_createdAt"))
})
public class PendingNotificationModel {
    @Id
    private ObjectId id;
    private UUID recipient;
    private String message;
    private Instant createdAt;
}
