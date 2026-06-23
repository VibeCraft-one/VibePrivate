package com.vibeprivate.service;

import java.util.Objects;

public final class UpgradeResult {
    private final UpgradeStatus status;
    private final int consumedAmount;
    private final int oldRadius;
    private final int newRadius;

    private UpgradeResult(UpgradeStatus status, int consumedAmount, int oldRadius, int newRadius) {
        this.status = Objects.requireNonNull(status, "status");
        this.consumedAmount = Math.max(0, consumedAmount);
        this.oldRadius = Math.max(0, oldRadius);
        this.newRadius = Math.max(0, newRadius);
    }

    public static UpgradeResult success(int consumedAmount, int oldRadius, int newRadius) {
        return new UpgradeResult(UpgradeStatus.SUCCESS, consumedAmount, oldRadius, newRadius);
    }

    public static UpgradeResult fail(UpgradeStatus status, int currentRadius) {
        if (status == UpgradeStatus.SUCCESS) {
            throw new IllegalArgumentException("Use success() for successful upgrades.");
        }

        return new UpgradeResult(status, 0, currentRadius, currentRadius);
    }

    public UpgradeStatus getStatus() {
        return status;
    }

    public int getConsumedAmount() {
        return consumedAmount;
    }

    public int getOldRadius() {
        return oldRadius;
    }

    public int getNewRadius() {
        return newRadius;
    }
}
