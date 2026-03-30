package de.interwebmedia.report.bungee.platform;

import de.interwebmedia.report.api.service.PlatformAsyncExecutor;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeAsyncExecutor implements PlatformAsyncExecutor {

    private final Plugin plugin;

    public BungeeAsyncExecutor(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier) {
        CompletableFuture<T> future = new CompletableFuture<>();
        plugin.getProxy().getScheduler().runAsync(plugin, () -> complete(future, supplier));
        return future;
    }

    @Override
    public CompletableFuture<Void> runAsync(Runnable runnable) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
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
