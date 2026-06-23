package com.vibeprivate.protection;

import com.vibeprivate.message.MessageService;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class ProtectionDenyNotifier {
    private static final long DENY_MESSAGE_COOLDOWN_MILLIS = 60_000L;

    private final ProtectionService protectionService;
    private final MessageService messageService;
    private final Map<UUID, Long> nextDenyMessageAtByPlayer = new HashMap<>();

    public ProtectionDenyNotifier(ProtectionService protectionService, MessageService messageService) {
        this.protectionService = Objects.requireNonNull(protectionService, "protectionService");
        this.messageService = Objects.requireNonNull(messageService, "messageService");
    }

    public void sendDenied(Player player, Location location) {
        if (!protectionService.shouldNotifyDenied(player, location)) {
            return;
        }

        long now = System.currentTimeMillis();
        long nextMessageAt = nextDenyMessageAtByPlayer.getOrDefault(player.getUniqueId(), 0L);
        if (nextMessageAt > now) {
            return;
        }

        nextDenyMessageAtByPlayer.put(player.getUniqueId(), now + DENY_MESSAGE_COOLDOWN_MILLIS);
        messageService.send(player, "protection.denied");
    }
}
