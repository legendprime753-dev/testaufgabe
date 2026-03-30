package de.interwebmedia.report.api.dto;

import de.interwebmedia.report.api.model.ReportStatus;
import java.time.Instant;
import java.util.UUID;

public record StatusUpdateMessage(
        String reportId,
        UUID reporterUuid,
        String reporterName,
        String reportedName,
        ReportStatus status,
        String handledBy,
        Instant timestamp
) {
}
