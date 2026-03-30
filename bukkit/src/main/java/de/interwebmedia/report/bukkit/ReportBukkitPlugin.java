package de.interwebmedia.report.bukkit;

import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import de.interwebmedia.report.api.service.ReportService;
import de.interwebmedia.report.api.service.UuidLookupService;
import de.interwebmedia.report.bukkit.command.ReportCommand;
import de.interwebmedia.report.bukkit.platform.BukkitAsyncExecutor;
import de.interwebmedia.report.common.config.ReportSystemConfig;
import de.interwebmedia.report.common.http.McApiClient;
import de.interwebmedia.report.common.messaging.RedisPublisher;
import de.interwebmedia.report.common.service.ReportServiceImpl;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.mapping.MapperOptions;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
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

        getCommand("report").setExecutor(new ReportCommand(reportService, lookupService, config.bedrockPrefix()));
        getLogger().info("Report system enabled.");
    }

    @Override
    public void onDisable() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }
}
