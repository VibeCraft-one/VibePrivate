package com.vibeprivate.service;

public record RegionSelectionWorldHeight(int minY, int maxY) {
    public RegionSelectionWorldHeight {
        if (maxY < minY) {
            throw new IllegalArgumentException("maxY must be greater than or equal to minY.");
        }
    }
}
