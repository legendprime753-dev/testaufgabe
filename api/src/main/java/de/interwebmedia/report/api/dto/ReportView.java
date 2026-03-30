package de.interwebmedia.report.api.dto;

import de.interwebmedia.report.api.model.ReportStatus;
import java.time.Instant;
import java.util.UUID;

public record ReportView(
        String id,
        UUID reporter,
        UUID reported,
        String reporterName,
        String reportedName,
        String reason,
        ReportStatus status,
        UUID handledBy,
        String handledByName,
        String modNote,
        String server,
        Instant createdAt,
        Instant handledAt
) {
}
