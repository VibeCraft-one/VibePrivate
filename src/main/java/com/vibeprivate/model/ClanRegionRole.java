package com.vibeprivate.model;

public enum ClanRegionRole {
    LEADER,
    OFFICER,
    MEMBER;

    public boolean canManageClanRegion() {
        return this == LEADER || this == OFFICER;
    }

    public boolean isElevated() {
        return this == LEADER || this == OFFICER;
    }
}
