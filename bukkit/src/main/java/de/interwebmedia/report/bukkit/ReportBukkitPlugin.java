package de.interwebmedia.report.bukkit;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import de.interwebmedia.report.api.service.ReportService;
import de.interwebmedia.report.api.service.UuidLookupService;
import de.interwebmedia.report.bukkit.command.ReportCommand;
import de.interwebmedia.report.bukkit.command.ReportsCommand;
import de.interwebmedia.report.bukkit.listener.PendingNotificationLoginListener;
import de.interwebmedia.report.bukkit.listener.ReportNotificationListener;
import de.interwebmedia.report.bukkit.platform.BukkitAsyncExecutor;
import de.interwebmedia.report.bukkit.service.StatusUpdateHandler;
import de.interwebmedia.report.common.config.ReportSystemConfig;
import de.interwebmedia.report.common.http.McApiClient;
import de.interwebmedia.report.common.messaging.RedisChannels;
import de.interwebmedia.report.common.messaging.RedisPublisher;
import de.interwebmedia.report.common.messaging.RedisSubscriber;
import de.interwebmedia.report.common.service.PendingNotificationService;
import de.interwebmedia.report.common.service.ReportServiceImpl;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import org.bukkit.plugin.java.JavaPlugin;

public class ReportBukkitPlugin extends JavaPlugin {

    private MongoClient mongoClient;

    @Override
    public void onEnable() {
        ReportSystemConfig config = ReportSystemConfig.defaults();

        mongoClient = MongoClients.create(config.mongoUri());
        Datastore datastore = Morphia.createDatastore(mongoClient, config.mongoDatabase(), MapperOptions.builder().build());
        datastore.getMapper().mapPackage("de.interwebmedia.report.common.mongo");
        datastore.ensureIndexes();

        PlatformAsyncExecutor asyncExecutor = new BukkitAsyncExecutor(this);
        RedisPublisher publisher = new RedisPublisher(config.redisHost(), config.redisPort());
        ReportService reportService = new ReportServiceImpl(datastore, publisher, asyncExecutor);
        UuidLookupService lookupService = new McApiClient(config.uuidCacheTtl());
        PendingNotificationService pendingNotificationService = new PendingNotificationService(datastore, asyncExecutor);

        getCommand("report").setExecutor(new ReportCommand(reportService, lookupService, config.bedrockPrefix()));
        getCommand("reports").setExecutor(new ReportsCommand(reportService));

        getServer().getPluginManager().registerEvents(new PendingNotificationLoginListener(pendingNotificationService), this);

        ReportNotificationListener adminNotifier = new ReportNotificationListener();
        StatusUpdateHandler statusUpdateHandler = new StatusUpdateHandler(pendingNotificationService);
        RedisSubscriber subscriber = new RedisSubscriber(config.redisHost(), config.redisPort());

        getServer().getScheduler().runTaskAsynchronously(this, () ->
                subscriber.subscribe((channel, payload) -> {
                    if (RedisChannels.NEW_REPORT.equals(channel)) {
                        adminNotifier.broadcastToAdmins("[REPORT] Neuer Report eingegangen. Nutze /reports.");
                    }
                    if (RedisChannels.STATUS_UPDATE.equals(channel)) {
                        statusUpdateHandler.handle(payload);
                    }
                }, RedisChannels.NEW_REPORT, RedisChannels.STATUS_UPDATE));

        getLogger().info("Report system enabled.");
    }

    @Override
    public void onDisable() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
