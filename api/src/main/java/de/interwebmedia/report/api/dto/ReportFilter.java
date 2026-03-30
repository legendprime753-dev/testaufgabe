package de.interwebmedia.report.api.dto;

import de.interwebmedia.report.api.model.ReportStatus;
import java.time.Instant;
import java.util.Set;

public record ReportFilter(
        Set<ReportStatus> statuses,
        Set<String> servers,
        Set<String> templates,
        Instant createdAfter,
        Instant createdBefore,
        int limit
) {
    public static ReportFilter openDefault(int limit) {
        return new ReportFilter(Set.of(ReportStatus.OPEN), Set.of(), Set.of(), null, null, limit);
    }
}
