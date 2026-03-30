package de.interwebmedia.report.api.service;

import de.interwebmedia.report.api.model.PlayerEdition;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UuidLookupService {
    CompletableFuture<UUID> lookup(String username, PlayerEdition edition);
}
