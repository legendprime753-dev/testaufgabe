package de.interwebmedia.report.bungee;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import de.interwebmedia.report.api.service.ReportService;
import de.interwebmedia.report.bungee.listener.AdminLoginListener;
import de.interwebmedia.report.bungee.platform.BungeeAsyncExecutor;
import de.interwebmedia.report.bungee.service.AdminReminderService;
import de.interwebmedia.report.common.config.ReportSystemConfig;
import de.interwebmedia.report.common.messaging.RedisPublisher;
import de.interwebmedia.report.common.service.ReportServiceImpl;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import net.md_5.bungee.api.plugin.Plugin;

public class ReportBungeePlugin extends Plugin {

    private MongoClient mongoClient;

    @Override
    public void onEnable() {
        ReportSystemConfig config = ReportSystemConfig.defaults();
        mongoClient = MongoClients.create(config.mongoUri());

        Datastore datastore = Morphia.createDatastore(mongoClient, config.mongoDatabase(), MapperOptions.builder().build());
        datastore.getMapper().mapPackage("de.interwebmedia.report.common.mongo");

        PlatformAsyncExecutor asyncExecutor = new BungeeAsyncExecutor(this);
        RedisPublisher publisher = new RedisPublisher(config.redisHost(), config.redisPort());
        ReportService reportService = new ReportServiceImpl(datastore, publisher, asyncExecutor);

        getProxy().getPluginManager().registerListener(this, new AdminLoginListener(new AdminReminderService(reportService)));
        getLogger().info("Report Bungee module enabled.");
    }

    @Override
    public void onDisable() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
