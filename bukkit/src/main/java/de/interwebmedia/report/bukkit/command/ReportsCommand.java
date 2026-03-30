package de.interwebmedia.report.bukkit.command;

import de.interwebmedia.report.api.dto.ModerationActionRequest;
import de.interwebmedia.report.api.dto.ReportFilter;
import de.interwebmedia.report.api.model.ReportStatus;
import de.interwebmedia.report.api.service.ReportService;
import java.util.UUID;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReportsCommand implements CommandExecutor {

    private final ReportService reportService;

    public ReportsCommand(ReportService reportService) {
        this.reportService = reportService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("report.admin")) {
            sender.sendMessage("Keine Berechtigung.");
            return true;
        }

        if (args.length == 0) {
            reportService.list(ReportFilter.openDefault(10))
                    .thenAccept(reports -> {
                        sender.sendMessage("§6Offene Reports (Top 10):");
                        reports.forEach(report -> sender.sendMessage("§e#" + report.id() + " §7" + report.reportedName() + " §8(" + report.reason() + ")"));
                    });
            return true;
        }

        return switch (args[0].toLowerCase()) {
            case "resolve" -> moderate(sender, args, ReportStatus.RESOLVED);
            case "reject" -> moderate(sender, args, ReportStatus.REJECTED);
            case "details" -> details(sender, args);
            case "stats" -> stats(sender, args);
            default -> {
                sender.sendMessage("Usage: /reports [resolve|reject|details|stats]");
                yield true;
            }
        };
    }

    private boolean moderate(CommandSender sender, String[] args, ReportStatus status) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /reports " + status.name().toLowerCase() + " <id> [grund]");
            return true;
        }
        String id = args[1];
        String note = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "";
        UUID moderatorUuid = sender instanceof Player p ? p.getUniqueId() : null;
        reportService.moderate(new ModerationActionRequest(id, status, moderatorUuid, sender.getName(), note))
                .thenRun(() -> sender.sendMessage("Report " + id + " -> " + status))
                .exceptionally(ex -> {
                    sender.sendMessage("Fehler: " + ex.getMessage());
                    return null;
                });
        return true;
    }

    private boolean details(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /reports details <id>");
            return true;
        }
        reportService.details(args[1])
                .thenAccept(optional -> optional.ifPresentOrElse(report -> {
                    sender.sendMessage("§6Report #" + report.id());
                    sender.sendMessage("§7Reporter: §f" + report.reporterName());
                    sender.sendMessage("§7Reported: §f" + report.reportedName());
                    sender.sendMessage("§7Reason: §f" + report.reason());
                    sender.sendMessage("§7Status: §f" + report.status());
                    sender.sendMessage("§7Server: §f" + report.server());
                }, () -> sender.sendMessage("Report nicht gefunden.")));
        return true;
    }

    private boolean stats(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /reports stats <player>");
            return true;
        }
        reportService.stats(args[1]).thenAccept(stats -> sender.sendMessage(
                "§6Stats " + stats.playerName() + ": total=" + stats.total() + ", open=" + stats.open() + ", resolved=" + stats.resolved() + ", rejected=" + stats.rejected()));
        return true;
    }
}
