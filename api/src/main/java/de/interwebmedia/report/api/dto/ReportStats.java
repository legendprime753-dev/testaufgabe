package de.interwebmedia.report.api.dto;

public record ReportStats(
        String playerName,
        long total,
        long open,
        long resolved,
        long rejected
) {
}
