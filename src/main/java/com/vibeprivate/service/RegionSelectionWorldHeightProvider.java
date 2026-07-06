package com.vibeprivate.service;

import java.util.Optional;

public interface RegionSelectionWorldHeightProvider {
    Optional<RegionSelectionWorldHeight> getWorldHeight(String worldName);
}
