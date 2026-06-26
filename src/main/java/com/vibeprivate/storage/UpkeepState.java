package com.vibeprivate.storage;

public record UpkeepState(String ownerId, int debtDays, long lastChargedAt) {
    public static UpkeepState empty(String ownerId) {
        return new UpkeepState(ownerId, 0, 0L);
    }
}
