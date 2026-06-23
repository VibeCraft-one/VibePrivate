package com.vibeprivate.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class CommandCooldownService {
    private final Map<String, Long> nextUseByKey = new HashMap<>();

    public long getRemainingMillis(UUID playerId, String command) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(command, "command");
        long now = System.currentTimeMillis();
        long nextUse = nextUseByKey.getOrDefault(key(playerId, command), 0L);
        return Math.max(0L, nextUse - now);
    }

    public void markUsed(UUID playerId, String command, int cooldownSeconds) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(command, "command");
        nextUseByKey.put(key(playerId, command), System.currentTimeMillis() + Math.max(0, cooldownSeconds) * 1000L);
    }

    private String key(UUID playerId, String command) {
        return playerId + ":" + command.toLowerCase(java.util.Locale.ROOT);
    }
}
