package com.vibeprivate.gui;

import com.vibeprivate.message.MessageService;
import com.vibeprivate.model.Region;
import com.vibeprivate.service.RegionAccessService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class RegionMembersMenu implements InventoryHolder {
    public static final int BACK_SLOT = 53;
    private static final int INFO_SLOT = 4;
    private static final int CURRENT_HEADER_SLOT = 9;
    private static final int ADD_HEADER_SLOT = 27;
    private static final int HELP_SLOT = 49;
    private static final int[] CURRENT_MEMBER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16, 17,
            19, 20, 21, 22, 23, 24, 25, 26
    };
    private static final int[] ADD_MEMBER_SLOTS = {
            28, 29, 30, 31, 32, 33, 34, 35,
            37, 38, 39, 40, 41, 42, 43, 44
    };

    private final MessageService messageService;
    private final RegionAccessService accessService;
    private final Player owner;
    private final Region region;
    private final Inventory inventory;
    private final GuiItemFactory itemFactory;
    private final Map<Integer, UUID> addSlots = new HashMap<>();
    private final Map<Integer, UUID> removeSlots = new HashMap<>();
    private final Map<Integer, UUID> editSlots = new HashMap<>();

    public RegionMembersMenu(MessageService messageService, RegionAccessService accessService,
                             Player owner, Region region) {
        this.messageService = Objects.requireNonNull(messageService, "messageService");
        this.accessService = Objects.requireNonNull(accessService, "accessService");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.region = Objects.requireNonNull(region, "region");
        this.itemFactory = new GuiItemFactory(messageService);
        this.inventory = Bukkit.createInventory(this, 54, messageService.get("gui.members.title"));
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

    public UUID getAddPlayerId(int slot) {
        return addSlots.get(slot);
    }

    public UUID getRemovePlayerId(int slot) {
        return removeSlots.get(slot);
    }

    public UUID getEditPlayerId(int slot) {
        return editSlots.get(slot);
    }

    private void render() {
        addSlots.clear();
        removeSlots.clear();
        editSlots.clear();

        Set<UUID> members = accessService.getMembers(region.getId());
        inventory.setItem(INFO_SLOT, infoItem(members.size()));
        inventory.setItem(CURRENT_HEADER_SLOT, simpleItem(Material.REDSTONE_TORCH,
                "gui.members.current.name",
                List.of("gui.members.current.lore.1", "gui.members.current.lore.2")));
        inventory.setItem(ADD_HEADER_SLOT, simpleItem(Material.LIME_DYE,
                "gui.members.add-section.name",
                List.of("gui.members.add-section.lore.1", "gui.members.add-section.lore.2")));
        inventory.setItem(HELP_SLOT, simpleItem(Material.WRITABLE_BOOK,
                "gui.members.help.name",
                List.of("gui.members.help.lore.1", "gui.members.help.lore.2", "gui.members.help.lore.3")));
        inventory.setItem(BACK_SLOT, simpleItem(Material.ARROW, "gui.back.name", List.of("gui.back.lore")));

        renderCurrentMembers(members);
        renderAddCandidates(members);
    }

    private void renderCurrentMembers(Set<UUID> members) {
        if (members.isEmpty()) {
            inventory.setItem(CURRENT_MEMBER_SLOTS[0], simpleItem(Material.GRAY_DYE,
                    "gui.members.empty-current.name",
                    List.of("gui.members.empty-current.lore.1", "gui.members.empty-current.lore.2")));
            return;
        }

        List<UUID> sortedMembers = new ArrayList<>(members);
        sortedMembers.sort(Comparator.comparing(this::playerName, String.CASE_INSENSITIVE_ORDER));

        for (int index = 0; index < sortedMembers.size() && index < CURRENT_MEMBER_SLOTS.length; index++) {
            UUID memberId = sortedMembers.get(index);
            int slot = CURRENT_MEMBER_SLOTS[index];
            inventory.setItem(slot, playerItem(memberId, "gui.members.edit.name",
                    List.of("gui.members.edit.lore.1", "gui.members.edit.lore.2")));
            editSlots.put(slot, memberId);
        }
    }

    private void renderAddCandidates(Set<UUID> members) {
        List<Player> candidates = new ArrayList<>();
        for (Player online : Bukkit.getOnlinePlayers()) {
            UUID playerId = online.getUniqueId();
            if (!playerId.equals(owner.getUniqueId()) && !members.contains(playerId)) {
                candidates.add(online);
            }
        }
        candidates.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

        if (candidates.isEmpty()) {
            inventory.setItem(ADD_MEMBER_SLOTS[0], simpleItem(Material.GRAY_DYE,
                    "gui.members.empty-add.name",
                    List.of("gui.members.empty-add.lore.1", "gui.members.empty-add.lore.2")));
            return;
        }

        for (int index = 0; index < candidates.size() && index < ADD_MEMBER_SLOTS.length; index++) {
            Player candidate = candidates.get(index);
            int slot = ADD_MEMBER_SLOTS[index];
            inventory.setItem(slot, playerItem(candidate.getUniqueId(), "gui.members.add.name",
                    List.of("gui.members.add.lore.1", "gui.members.add.lore.2")));
            addSlots.put(slot, candidate.getUniqueId());
        }
    }

    private ItemStack infoItem(int count) {
        return itemFactory.itemWithLore(Material.NAME_TAG,
                messageService.get("gui.members.info.name"),
                List.of(
                messageService.get("gui.members.info.lore.1", Map.of(
                        "count", Integer.toString(count),
                        "region", region.getName()
                )),
                messageService.get("gui.members.info.edit-model.1"),
                messageService.get("gui.members.info.edit-model.2")
        ));
    }

    private ItemStack playerItem(UUID playerId, String nameKey, List<String> loreKeys) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        String name = playerName(playerId);
        return itemFactory.head(offlinePlayer, nameKey, Map.of("player", name), loreKeys, Map.of("player", name));
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
