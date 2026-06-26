package com.vibeprivate.service;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.lang.reflect.Method;
import java.util.Objects;

public final class EconomyService {
    private Object economy;
    private Method getBalance;
    private Method withdrawPlayer;
    private Method format;

    public void hook() {
        economy = null;
        getBalance = null;
        withdrawPlayer = null;
        format = null;

        try {
            Class<?> economyClass = Class.forName("net.milkbowl.vault.economy.Economy");
            RegisteredServiceProvider<?> registration = Bukkit.getServicesManager().getRegistration(economyClass);
            if (registration == null) {
                return;
            }

            economy = registration.getProvider();
            getBalance = economy.getClass().getMethod("getBalance", OfflinePlayer.class);
            withdrawPlayer = economy.getClass().getMethod("withdrawPlayer", OfflinePlayer.class, double.class);
            format = economy.getClass().getMethod("format", double.class);
        } catch (Throwable ignored) {
            economy = null;
        }
    }

    public boolean isAvailable() {
        return economy != null;
    }

    public double getBalance(OfflinePlayer player) {
        Objects.requireNonNull(player, "player");
        if (!isAvailable()) {
            return 0.0D;
        }

        try {
            return ((Number) getBalance.invoke(economy, player)).doubleValue();
        } catch (Throwable ignored) {
            return 0.0D;
        }
    }

    public boolean withdraw(OfflinePlayer player, double amount) {
        Objects.requireNonNull(player, "player");
        if (amount <= 0.0D) {
            return true;
        }
        if (!isAvailable()) {
            return false;
        }

        try {
            Object response = withdrawPlayer.invoke(economy, player, amount);
            Method transactionSuccess = response.getClass().getMethod("transactionSuccess");
            return Boolean.TRUE.equals(transactionSuccess.invoke(response));
        } catch (Throwable ignored) {
            return false;
        }
    }

    public String format(double amount) {
        if (!isAvailable()) {
            return String.format("%.2f", amount);
        }

        try {
            return Objects.toString(format.invoke(economy, amount), String.format("%.2f", amount));
        } catch (Throwable ignored) {
            return String.format("%.2f", amount);
        }
    }
}
