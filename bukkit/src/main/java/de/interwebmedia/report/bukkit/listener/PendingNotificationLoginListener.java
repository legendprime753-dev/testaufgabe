package de.interwebmedia.report.bukkit.listener;

import de.interwebmedia.report.common.service.PendingNotificationService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PendingNotificationLoginListener implements Listener {

    private final PendingNotificationService pendingNotificationService;

    public PendingNotificationLoginListener(PendingNotificationService pendingNotificationService) {
        this.pendingNotificationService = pendingNotificationService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        pendingNotificationService.pull(event.getPlayer().getUniqueId())
                .thenAccept(entries -> entries.forEach(entry -> event.getPlayer().sendMessage(entry.getMessage())));
    }
}
