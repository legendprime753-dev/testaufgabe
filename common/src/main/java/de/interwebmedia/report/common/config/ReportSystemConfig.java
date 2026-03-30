package de.interwebmedia.report.common.config;

import java.time.Duration;

public record ReportSystemConfig(
        String mongoUri,
        String mongoDatabase,
        String redisHost,
        int redisPort,
        String bedrockPrefix,
        Duration uuidCacheTtl
) {
    public static ReportSystemConfig defaults() {
        return new ReportSystemConfig(
                "mongodb://localhost:27017",
                "report_system",
                "localhost",
                6379,
                ".",
                Duration.ofMinutes(20)
        );
    }
}
