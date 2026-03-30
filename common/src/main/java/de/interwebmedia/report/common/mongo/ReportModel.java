package de.interwebmedia.report.common.mongo;

import de.interwebmedia.report.api.model.ReportStatus;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
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
public class ReportModel {
    @Id
    private ObjectId id;
    private UUID reporter;
    private UUID reported;
    private String reporterName;
    private String reportedName;
    private String reason;
    private ReportStatus status;
    private UUID handledBy;
    private String handledByName;
    private String modNote;
    private String server;
    private Instant createdAt;
    private Instant handledAt;
}
