package de.interwebmedia.report.api.dto;

import de.interwebmedia.report.api.model.ReportTemplate;
import java.util.UUID;

public record ReportCreateRequest(
        UUID reporterUuid,
        String reporterName,
        UUID reportedUuid,
        String reportedName,
        ReportTemplate template,
        String customReason,
        String server
) {
    public String reasonToStore() {
        return template.requiresCustomText() ? customReason : template.name();
    }
}
