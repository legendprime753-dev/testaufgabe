package de.interwebmedia.report.bukkit.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.interwebmedia.report.common.service.PendingNotificationService;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class StatusUpdateHandler {

    private final PendingNotificationService pendingNotificationService;

    public StatusUpdateHandler(PendingNotificationService pendingNotificationService) {
        this.pendingNotificationService = pendingNotificationService;
    }

    public void handle(String payload) {
        JsonObject root = JsonParser.parseString(payload).getAsJsonObject();
        UUID reporterUuid = UUID.fromString(root.get("reporterUuid").getAsString());
        String reporterName = root.get("reporterName").getAsString();
        String reportedName = root.get("reportedName").getAsString();
        String status = root.get("status").getAsString();
        String modNote = root.has("modNote") ? root.get("modNote").getAsString() : "";

        String message = switch (status) {
            case "RESOLVED" -> "[REPORT] Dein Report gegen " + reportedName + " wurde bearbeitet und als berechtigt eingestuft.";
            case "REJECTED" -> "[REPORT] Dein Report gegen " + reportedName + " wurde geprüft und abgelehnt. Grund: " + modNote;
            default -> "[REPORT] Dein Report gegen " + reportedName + " wurde aktualisiert.";
        };

        Player player = Bukkit.getPlayer(reporterUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage(message);
            return;
        }

        pendingNotificationService.store(reporterUuid, message);
        Bukkit.getLogger().fine("Stored pending report update for " + reporterName);
    }
}
