package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.service.RegionAccessService;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

final class FlagItemFactory {
    private final MessageService messageService;
    private final RegionAccessService accessService;
    private final GuiItemFactory itemFactory;

    FlagItemFactory(MessageService messageService, RegionAccessService accessService, GuiItemFactory itemFactory) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.accessService = Objects.requireNonNull(accessService, "accessService");
        this.itemFactory = Objects.requireNonNull(itemFactory, "itemFactory");
    }

    ItemStack regionFlagItem(Region region, RegionFlag flag) {
        boolean enabled = accessService.getDefaultFlag(region.getId(), flag);
        return itemFactory.itemWithLore(FlagView.material(flag),
                messageService.get("gui.region-detail.flag.name", Map.of(
                        "flag", messageService.get(FlagView.nameKey(flag))
                )),
                List.of(
                        messageService.get(regionFlagStateKey(region, enabled)),
                        messageService.get(FlagView.descriptionKey(flag)),
                        messageService.get("gui.region-detail.flag.lore")
                ));
    }

    ItemStack memberFlagItem(Region region, UUID memberId, RegionFlag flag) {
        boolean effectiveEnabled = accessService.getMemberEffectiveFlag(region, memberId, flag);
        boolean custom = accessService.hasMemberFlag(region.getId(), memberId, flag);
        return itemFactory.itemWithLore(FlagView.material(flag),
                messageService.get("gui.member-flags.flag.name", Map.of(
                        "flag", messageService.get(FlagView.nameKey(flag))
                )),
                List.of(
                        messageService.get(memberFlagStateKey(custom, effectiveEnabled)),
                        messageService.get(FlagView.descriptionKey(flag)),
                        messageService.get("gui.member-flags.flag.lore")
                ));
    }

    private String regionFlagStateKey(Region region, boolean enabled) {
        if (region.isAdmin()) {
            return enabled ? "gui.region-detail.flag.guest-enabled" : "gui.region-detail.flag.guest-disabled";
        }

        return enabled ? "gui.region-detail.flag.enabled" : "gui.region-detail.flag.disabled";
    }

    private String memberFlagStateKey(boolean custom, boolean enabled) {
        if (custom) {
            return enabled ? "gui.member-flags.flag.custom-enabled" : "gui.member-flags.flag.custom-disabled";
        }

        return enabled ? "gui.member-flags.flag.inherited-enabled" : "gui.member-flags.flag.inherited-disabled";
    }
}
