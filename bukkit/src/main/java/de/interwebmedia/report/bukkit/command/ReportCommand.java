package de.interwebmedia.report.bukkit.command;

import de.interwebmedia.report.api.dto.ReportCreateRequest;
import de.interwebmedia.report.api.model.PlayerEdition;
import de.interwebmedia.report.api.model.ReportTemplate;
import de.interwebmedia.report.api.service.ReportService;
import de.interwebmedia.report.api.service.UuidLookupService;
import java.util.Locale;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReportCommand implements CommandExecutor {

    private final ReportService reportService;
    private final UuidLookupService uuidLookupService;
    private final String bedrockPrefix;

    public ReportCommand(ReportService reportService, UuidLookupService uuidLookupService, String bedrockPrefix) {
        this.reportService = reportService;
        this.uuidLookupService = uuidLookupService;
        this.bedrockPrefix = bedrockPrefix;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player reporter)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            reporter.sendMessage("Usage: /report <player> [CHEATING|INSULT|BUGUSING|GRIEFING|SPAM|OTHER] [text]");
            return true;
        }

        String targetName = args[0];
        ReportTemplate template = parseTemplate(args.length > 1 ? args[1] : "OTHER");
        String customText = args.length > 2 ? String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)) : "";

        Player onlineTarget = Bukkit.getPlayerExact(targetName);
        if (onlineTarget != null) {
            createAndConfirm(reporter, onlineTarget.getUniqueId(), onlineTarget.getName(), template, customText);
            return true;
        }

        PlayerEdition edition = targetName.startsWith(bedrockPrefix) ? PlayerEdition.BEDROCK : PlayerEdition.JAVA;
        uuidLookupService.lookup(targetName, edition)
                .thenCompose(uuid -> reportService.createReport(new ReportCreateRequest(
                        reporter.getUniqueId(),
                        reporter.getName(),
                        uuid,
                        targetName,
                        template,
                        customText,
                        reporter.getServer().getName()
                )))
                .thenAccept(reportId -> reporter.sendMessage("Dein Report wurde aufgenommen. ID: " + reportId))
                .exceptionally(ex -> {
                    reporter.sendMessage("Spieler konnte nicht aufgelöst werden oder API ist nicht verfügbar.");
                    return null;
                });
        return true;
    }

    private ReportTemplate parseTemplate(String raw) {
        try {
            return ReportTemplate.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return ReportTemplate.OTHER;
        }
    }

    private void createAndConfirm(Player reporter, UUID reportedUuid, String reportedName, ReportTemplate template, String customText) {
        reportService.createReport(new ReportCreateRequest(
                reporter.getUniqueId(),
                reporter.getName(),
                reportedUuid,
                reportedName,
                template,
                customText,
                reporter.getServer().getName()
        )).thenAccept(reportId -> reporter.sendMessage("Dein Report wurde aufgenommen. ID: " + reportId));
    }
}
