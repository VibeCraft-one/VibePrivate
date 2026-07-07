package com.vibeprivate.service;

import org.bukkit.plugin.PluginManager;

import java.util.Objects;

public final class BukkitRegionEventDispatcher implements RegionEventDispatcher {
    private final PluginManager pluginManager;

    public BukkitRegionEventDispatcher(PluginManager pluginManager) {
        this.pluginManager = Objects.requireNonNull(pluginManager, "pluginManager");
    }

    @Override
    public void dispatch(org.bukkit.event.Event event) {
        pluginManager.callEvent(Objects.requireNonNull(event, "event"));
    }
}
