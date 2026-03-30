package de.interwebmedia.report.bungee.service;

import de.interwebmedia.report.api.service.ReportService;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class AdminReminderService {

    private final ReportService reportService;

    public AdminReminderService(ReportService reportService) {
        this.reportService = reportService;
    }

    public void remind(ProxiedPlayer player) {
        if (!player.hasPermission("report.admin")) {
            return;
        }

        reportService.openReportCount()
                .thenAccept(count -> {
                    if (count > 0) {
                        player.sendMessage("[REPORT] Du hast aktuell " + count + " offene Reports. Nutze /reports.");
                    }
                });
    }
}
