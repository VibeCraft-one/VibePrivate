package com.vibeprivate.service;

import java.util.UUID;

public interface ClanRegionMembershipStore {
    boolean isMember(String regionId, UUID playerId);
}
