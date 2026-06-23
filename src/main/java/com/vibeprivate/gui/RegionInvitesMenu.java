package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.service.RegionInviteService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class RegionInvitesMenu implements InventoryHolder {
    public static final int BACK_SLOT = 26;

    private final MessageService messageService;
    private final RegionInviteService inviteService;
    private final Player player;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, String> regionIdsBySlot = new HashMap<>();

    public RegionInvitesMenu(MessageService messageService, RegionInviteService inviteService, Player player) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.inviteService = Objects.requireNonNull(inviteService, "inviteService");
        this.player = Objects.requireNonNull(player, "player");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 27, messageService.get("gui.invites.title"));
        render();
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public String getRegionId(int slot) {
        return regionIdsBySlot.get(slot);
    }

    private void render() {
        List<RegionInviteService.PendingInvite> invites = inviteService.getInvites(player.getUniqueId());
        int slot = 0;
        for (RegionInviteService.PendingInvite invite : invites) {
            if (slot >= 18) {
                break;
            }

            inventory.setItem(slot, inviteItem(invite));
            regionIdsBySlot.put(slot, invite.regionId());
            slot++;
        }

        if (invites.isEmpty()) {
            inventory.setItem(13, itemFactory.item(Material.GRAY_DYE, "gui.invites.empty.name",
                    List.of("gui.invites.empty.lore")));
        }

        inventory.setItem(BACK_SLOT, itemFactory.item(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));
    }

    private ItemStack inviteItem(RegionInviteService.PendingInvite invite) {
        return itemFactory.itemWithLore(Material.NAME_TAG, messageService.get("gui.invites.invite.name", Map.of(
                        "region", invite.regionName()
                )),
                List.of(
                        messageService.get("gui.invites.invite.lore.owner", Map.of("owner", invite.ownerName())),
                        messageService.get("gui.invites.invite.lore.accept"),
                        messageService.get("gui.invites.invite.lore.decline")
                ));
    }
}
