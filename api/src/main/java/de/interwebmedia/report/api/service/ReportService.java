package de.interwebmedia.report.api.service;

import de.interwebmedia.report.api.dto.ModerationActionRequest;
import de.interwebmedia.report.api.dto.ReportCreateRequest;
import de.interwebmedia.report.api.dto.ReportFilter;
import de.interwebmedia.report.api.dto.ReportStats;
import de.interwebmedia.report.api.dto.ReportView;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Main API for report lifecycle operations.
 */
public interface ReportService {

    CompletableFuture<String> createReport(ReportCreateRequest request);

    CompletableFuture<Void> moderate(ModerationActionRequest request);

    CompletableFuture<Optional<ReportView>> details(String reportId);

    CompletableFuture<List<ReportView>> list(ReportFilter filter);

    CompletableFuture<ReportStats> stats(String playerName);

    CompletableFuture<Long> openReportCount();
}
