package com.vibeprivate.service;

import java.util.Objects;

public final class FuelAddResult {
    private final FuelAddStatus status;
    private final int consumedAmount;
    private final long addedMinutes;
    private final long remainingMillis;

    private FuelAddResult(FuelAddStatus status, int consumedAmount, long addedMinutes, long remainingMillis) {
        this.status = Objects.requireNonNull(status, "status");
        this.consumedAmount = Math.max(0, consumedAmount);
        this.addedMinutes = Math.max(0, addedMinutes);
        this.remainingMillis = Math.max(0, remainingMillis);
    }

    public static FuelAddResult success(int consumedAmount, long addedMinutes, long remainingMillis) {
        return new FuelAddResult(FuelAddStatus.SUCCESS, consumedAmount, addedMinutes, remainingMillis);
    }

    public static FuelAddResult fail(FuelAddStatus status, long remainingMillis) {
        if (status == FuelAddStatus.SUCCESS) {
            throw new IllegalArgumentException("Use success() for successful fuel additions.");
        }

        return new FuelAddResult(status, 0, 0, remainingMillis);
    }

    public FuelAddStatus getStatus() {
        return status;
    }

    public int getConsumedAmount() {
        return consumedAmount;
    }

    public long getAddedMinutes() {
        return addedMinutes;
    }

    public long getRemainingMillis() {
        return remainingMillis;
    }
}
