package com.vibeprivate.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ConfirmationService {
    private static final int REQUIRED_ATTEMPTS = 3;
    private static final long EXPIRES_AFTER_MILLIS = 60_000L;

    private final Map<String, ConfirmationState> statesByKey = new HashMap<>();

    public int confirm(UUID actorId, String action, String targetId) {
        return confirm(actorId, action, targetId, REQUIRED_ATTEMPTS);
    }

    public int confirm(UUID actorId, String action, String targetId, int requiredAttempts) {
        Objects.requireNonNull(actorId, "actorId");
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(targetId, "targetId");
        if (requiredAttempts <= 1) {
            return 0;
        }

        long now = System.currentTimeMillis();
        String key = actorId + ":" + action + ":" + targetId;
        ConfirmationState state = statesByKey.get(key);
        if (state == null || state.expiresAt < now) {
            state = new ConfirmationState(0, now + EXPIRES_AFTER_MILLIS);
        }

        int attempts = state.attempts + 1;
        if (attempts >= requiredAttempts) {
            statesByKey.remove(key);
            return 0;
        }

        statesByKey.put(key, new ConfirmationState(attempts, now + EXPIRES_AFTER_MILLIS));
        return requiredAttempts - attempts;
    }

    private record ConfirmationState(int attempts, long expiresAt) {
    }
}
