package com.vibeprivate.service;

import java.util.Objects;

public final class WithdrawDepositResult {
    private final WithdrawDepositStatus status;
    private final int returnedAmount;

    private WithdrawDepositResult(WithdrawDepositStatus status, int returnedAmount) {
        this.status = Objects.requireNonNull(status, "status");
        this.returnedAmount = Math.max(0, returnedAmount);
    }

    public static WithdrawDepositResult success(int returnedAmount) {
        return new WithdrawDepositResult(WithdrawDepositStatus.SUCCESS, returnedAmount);
    }

    public static WithdrawDepositResult fail(WithdrawDepositStatus status) {
        if (status == WithdrawDepositStatus.SUCCESS) {
            throw new IllegalArgumentException("Use success() for successful withdrawals.");
        }

        return new WithdrawDepositResult(status, 0);
    }

    public WithdrawDepositStatus getStatus() {
        return status;
    }

    public int getReturnedAmount() {
        return returnedAmount;
    }
}
