package de.interwebmedia.report.api.service;

import de.interwebmedia.report.api.dto.ReportCreateRequest;
import de.interwebmedia.report.api.model.ReportStatus;
import java.util.concurrent.CompletableFuture;

/**
 * Main API for report lifecycle operations.
 */
public interface ReportService {

    CompletableFuture<String> createReport(ReportCreateRequest request);

    CompletableFuture<Void> changeStatus(String reportId, ReportStatus status, String moderatorName, String moderatorNote);

    CompletableFuture<Long> openReportCount();
}
