package de.interwebmedia.report.api.model;

public enum ReportTemplate {
    CHEATING,
    INSULT,
    BUGUSING,
    GRIEFING,
    SPAM,
    OTHER;

    public boolean requiresCustomText() {
        return this == OTHER;
    }
}
