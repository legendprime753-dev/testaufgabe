package de.interwebmedia.report.api.dto;

import de.interwebmedia.report.api.model.ReportStatus;
import java.util.UUID;

public record ModerationActionRequest(
        String reportId,
        ReportStatus targetStatus,
        UUID moderatorUuid,
        String moderatorName,
        String moderatorNote
) {
}
