package com.vibeprivate.listener;

import com.vibeprivate.manager.RegionManager;
import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionType;
import com.vibeprivate.service.RegionHomeService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;

import java.util.Objects;

public final class RegionHomeListener implements Listener {
    private final RegionManager regionManager;
    private final RegionHomeService homeService;
    private final MessageService messageService;

    public RegionHomeListener(RegionManager regionManager, RegionHomeService homeService, MessageService messageService) {
        this.regionManager = Objects.requireNonNull(regionManager, "regionManager");
        this.homeService = Objects.requireNonNull(homeService, "homeService");
        this.messageService = Objects.requireNonNull(messageService, "messageService");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        Region region = regionManager.getRegionIncludingDisabledAtOrNull(player.getLocation());
        if (region == null || region.getType() != RegionType.HOME
                || !region.getOwnerId().equals(player.getUniqueId().toString())) {
            return;
        }

        if (homeService.setHome(player, region)) {
            messageService.send(player, "home.set.bed");
        }
    }
}
