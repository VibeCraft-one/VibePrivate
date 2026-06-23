package com.vibeprivate.service;

import java.util.Objects;

public final class DeletionResult {
    private final DeletionStatus status;
    private final int remainingConfirmations;

    private DeletionResult(DeletionStatus status, int remainingConfirmations) {
        this.status = Objects.requireNonNull(status, "status");
        this.remainingConfirmations = Math.max(0, remainingConfirmations);
    }

    public static DeletionResult confirmRequired(int remainingConfirmations) {
        return new DeletionResult(DeletionStatus.CONFIRM_REQUIRED, remainingConfirmations);
    }

    public static DeletionResult status(DeletionStatus status) {
        return new DeletionResult(status, 0);
    }

    public DeletionStatus getStatus() {
        return status;
    }

    public int getRemainingConfirmations() {
        return remainingConfirmations;
    }
}
