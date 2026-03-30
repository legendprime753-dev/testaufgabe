package de.interwebmedia.report.common.messaging;

public final class RedisChannels {
    public static final String NEW_REPORT = "reports:new";
    public static final String STATUS_UPDATE = "reports:status_update";

    private RedisChannels() {
    }
}
