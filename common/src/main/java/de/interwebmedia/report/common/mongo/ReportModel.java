package de.interwebmedia.report.common.mongo;

import de.interwebmedia.report.api.model.ReportStatus;
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
@Entity("reports")
@Indexes({
        @Index(fields = @Field("status"), options = @IndexOptions(name = "idx_status")),
        @Index(fields = @Field("reported"), options = @IndexOptions(name = "idx_reported")),
        @Index(fields = @Field("reporter"), options = @IndexOptions(name = "idx_reporter")),
        @Index(fields = @Field("createdAt"), options = @IndexOptions(name = "idx_createdAt"))
})
public class ReportModel {
    @Id
    private ObjectId id;
    private UUID reporter;
    private UUID reported;
    private String reporterName;
    private String reportedName;
    private String reason;
    private String template;
    private ReportStatus status;
    private UUID handledBy;
    private String handledByName;
    private String modNote;
    private String server;
    private Instant createdAt;
    private Instant handledAt;
}
