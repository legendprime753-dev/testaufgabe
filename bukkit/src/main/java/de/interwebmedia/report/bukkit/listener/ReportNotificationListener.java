package de.interwebmedia.report.bukkit.listener;

import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ReportNotificationListener {

    public void broadcastToAdmins(String message) {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();
        for (Player player : players) {
            if (player.hasPermission("report.admin")) {
                player.sendMessage(message);
            }
        }
    }
}
