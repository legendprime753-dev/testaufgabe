package de.interwebmedia.report.api.service;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Abstraction to route async execution through Bukkit/Bungee schedulers.
 */
public interface PlatformAsyncExecutor {
    <T> CompletableFuture<T> supplyAsync(Supplier<T> supplier);

    CompletableFuture<Void> runAsync(Runnable runnable);
}
