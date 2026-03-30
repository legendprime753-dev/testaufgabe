package de.interwebmedia.report.bukkit.platform;

import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitAsyncExecutor implements PlatformAsyncExecutor {

    private final JavaPlugin plugin;

    public BukkitAsyncExecutor(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> complete(future, supplier));
        return future;
    }

    @Override
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                runnable.run();
                future.complete(null);
            } catch (Exception exception) {
                future.completeExceptionally(exception);
            }
        });
        return future;
    }

    private <T> void complete(CompletableFuture<T> future, Supplier<T> supplier) {
        try {
            future.complete(supplier.get());
        } catch (Exception exception) {
            future.completeExceptionally(exception);
        }
    }
}
