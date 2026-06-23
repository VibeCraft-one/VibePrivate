package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.FlagCatalog;
import com.vibeprivate.model.Region;
import com.vibeprivate.model.RegionFlag;
import com.vibeprivate.service.RegionAccessService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class RegionMemberFlagsMenu implements InventoryHolder {
    public static final int BACK_SLOT = 49;
    public static final int REMOVE_SLOT = 53;

    private final MessageService messageService;
    private final RegionAccessService accessService;
    private final Player owner;
    private final Region region;
    private final UUID memberId;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final FlagItemFactory flagItemFactory;
    private final Map<Integer, RegionFlag> flagsBySlot = new HashMap<>();

    public RegionMemberFlagsMenu(MessageService messageService, RegionAccessService accessService,
                                 Player owner, Region region, UUID memberId) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.accessService = Objects.requireNonNull(accessService, "accessService");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.region = Objects.requireNonNull(region, "region");
        this.memberId = Objects.requireNonNull(memberId, "memberId");
        this.itemFactory = new GuiItemFactory(messageService);
        this.flagItemFactory = new FlagItemFactory(messageService, accessService, itemFactory);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.member-flags.title"));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getOwner() {
        return owner;
    }

    public Region getRegion() {
        return region;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public RegionFlag getFlag(int slot) {
        return flagsBySlot.get(slot);
    }

    private void render() {
        inventory.setItem(GuiSlots.INFO_SLOT, memberInfoItem());

        RegionFlag[] flags = FlagCatalog.memberFlags();
        for (int index = 0; index < flags.length && index < GuiSlots.MEMBER_FLAG_SLOTS.length; index++) {
            RegionFlag flag = flags[index];
            int slot = GuiSlots.MEMBER_FLAG_SLOTS[index];
            flagsBySlot.put(slot, flag);
            inventory.setItem(slot, flagItemFactory.memberFlagItem(region, memberId, flag));
        }

        inventory.setItem(BACK_SLOT, simpleItem(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
        inventory.setItem(REMOVE_SLOT, simpleItem(Material.BARRIER,
                "gui.member-flags.remove.name",
                List.of("gui.member-flags.remove.lore.1", "gui.member-flags.remove.lore.2")));
    }

    private ItemStack memberInfoItem() {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberId);
        String name = playerName(memberId);
        return itemFactory.headWithLore(offlinePlayer,
                messageService.get("gui.member-flags.info.name", Map.of("player", name)),
                List.of(
                messageService.get("gui.member-flags.info.lore.1", Map.of("region", region.getName())),
                messageService.get("gui.member-flags.info.lore.2"),
                messageService.get("gui.member-flags.info.lore.3")
        ));
    }

    private ItemStack simpleItem(Material material, String nameKey, List<String> loreKeys) {
        return itemFactory.item(material, nameKey, loreKeys);
    }

    private String playerName(UUID playerId) {
        Player online = Bukkit.getPlayer(playerId);
        if (online != null) {
            return online.getName();
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        String name = offlinePlayer.getName();
        return name == null ? playerId.toString().substring(0, 8) : name;
    }
}
