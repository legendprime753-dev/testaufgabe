package de.interwebmedia.report.bungee.listener;

import de.interwebmedia.report.bungee.service.AdminReminderService;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.api.plugin.Listener;

public class AdminLoginListener implements Listener {

    private final AdminReminderService adminReminderService;

    public AdminLoginListener(AdminReminderService adminReminderService) {
        this.adminReminderService = adminReminderService;
    }

    @EventHandler
    public void onLogin(PostLoginEvent event) {
        adminReminderService.remind(event.getPlayer());
    }
}
