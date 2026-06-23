package com.vibeprivate.model;

import java.util.Objects;

public final class Region {
    private final String id;
    private final String name;
    private final RegionType type;
    private final String ownerId;
    private final RegionShape shape;
    private final String worldName;
    private final Integer centerX;
    private final Integer centerZ;
    private final Integer radius;
    private final Integer pos1X;
    private final Integer pos1Y;
    private final Integer pos1Z;
    private final Integer pos2X;
    private final Integer pos2Y;
    private final Integer pos2Z;
    private final int minY;
    private final int maxY;
    private final long createdAt;
    private final RegionBounds bounds;
    private boolean enabled;
    private long fuelExpiresAt;
    private long fuelEmptySince;
    private long lastFuelDrainAt;
    private int upgradeLevel;
    private VisualizationMode visualizationMode;

    private Region(Builder builder) {
        id = requireText(builder.id, "id");
        name = requireText(builder.name, "name");
        type = Objects.requireNonNull(builder.type, "type");
        ownerId = requireText(builder.ownerId, "ownerId");
        shape = Objects.requireNonNull(builder.shape, "shape");
        worldName = requireText(builder.worldName, "worldName");
        centerX = builder.centerX;
        centerZ = builder.centerZ;
        radius = builder.radius;
        pos1X = builder.pos1X;
        pos1Y = builder.pos1Y;
        pos1Z = builder.pos1Z;
        pos2X = builder.pos2X;
        pos2Y = builder.pos2Y;
        pos2Z = builder.pos2Z;
        minY = builder.minY;
        maxY = builder.maxY;
        createdAt = builder.createdAt;
        enabled = builder.enabled;
        fuelExpiresAt = builder.fuelExpiresAt;
        fuelEmptySince = builder.fuelEmptySince;
        lastFuelDrainAt = builder.lastFuelDrainAt;
        upgradeLevel = Math.max(0, builder.upgradeLevel);
        visualizationMode = Objects.requireNonNull(builder.visualizationMode, "visualizationMode");
        bounds = createBounds();
    }

    public static Builder radiusRegion(String id, String name, RegionType type, String ownerId, String worldName) {
        if (type == RegionType.ADMIN) {
            throw new IllegalArgumentException("ADMIN regions must use cuboid bounds.");
        }

        return new Builder(id, name, type, ownerId, worldName, RegionShape.RADIUS);
    }

    public static Builder adminRegion(String id, String name, String ownerId, String worldName) {
        return new Builder(id, name, RegionType.ADMIN, ownerId, worldName, RegionShape.CUBOID);
    }

    public boolean contains(String worldName, int x, int y, int z) {
        return enabled && bounds.contains(worldName, x, y, z);
    }

    public boolean isAdmin() {
        return type == RegionType.ADMIN;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RegionType getType() {
        return type;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public RegionShape getShape() {
        return shape;
    }

    public String getWorldName() {
        return worldName;
    }

    public Integer getCenterX() {
        return centerX;
    }

    public Integer getCenterZ() {
        return centerZ;
    }

    public Integer getRadius() {
        return radius;
    }

    public Integer getPos1X() {
        return pos1X;
    }

    public Integer getPos1Y() {
        return pos1Y;
    }

    public Integer getPos1Z() {
        return pos1Z;
    }

    public Integer getPos2X() {
        return pos2X;
    }

    public Integer getPos2Y() {
        return pos2Y;
    }

    public Integer getPos2Z() {
        return pos2Z;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public RegionBounds getBounds() {
        return bounds;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getFuelExpiresAt() {
        return fuelExpiresAt;
    }

    public void setFuelExpiresAt(long fuelExpiresAt) {
        this.fuelExpiresAt = Math.max(0, fuelExpiresAt);
    }

    public long getFuelEmptySince() {
        return fuelEmptySince;
    }

    public void setFuelEmptySince(long fuelEmptySince) {
        this.fuelEmptySince = Math.max(0, fuelEmptySince);
    }

    public long getLastFuelDrainAt() {
        return lastFuelDrainAt;
    }

    public void setLastFuelDrainAt(long lastFuelDrainAt) {
        this.lastFuelDrainAt = Math.max(0, lastFuelDrainAt);
    }

    public int getUpgradeLevel() {
        return upgradeLevel;
    }

    public void setUpgradeLevel(int upgradeLevel) {
        this.upgradeLevel = Math.max(0, upgradeLevel);
    }

    public VisualizationMode getVisualizationMode() {
        return visualizationMode;
    }

    public void setVisualizationMode(VisualizationMode visualizationMode) {
        this.visualizationMode = Objects.requireNonNull(visualizationMode, "visualizationMode");
    }

    public Region withRadius(int newRadius) {
        if (shape != RegionShape.RADIUS) {
            throw new IllegalStateException("Only radius regions can be resized.");
        }

        return Region.radiusRegion(id, name, type, ownerId, worldName)
                .radius(requireInt(centerX, "centerX"), requireInt(centerZ, "centerZ"), newRadius, minY, maxY)
                .state(enabled, fuelExpiresAt, fuelEmptySince, lastFuelDrainAt, upgradeLevel, visualizationMode, createdAt)
                .build();
    }

    private RegionBounds createBounds() {
        if (shape == RegionShape.RADIUS) {
            return RegionBounds.radius(worldName, requireInt(centerX, "centerX"), requireInt(centerZ, "centerZ"),
                    requireInt(radius, "radius"), minY, maxY);
        }

        return new RegionBounds(
                worldName,
                requireInt(pos1X, "pos1X"),
                requireInt(pos2X, "pos2X"),
                requireInt(pos1Y, "pos1Y"),
                requireInt(pos2Y, "pos2Y"),
                requireInt(pos1Z, "pos1Z"),
                requireInt(pos2Z, "pos2Z")
        );
    }

    private static int requireInt(Integer value, String name) {
        if (value == null) {
            throw new IllegalArgumentException(name + " is required.");
        }

        return value;
    }

    private static String requireText(String value, String name) {
        Objects.requireNonNull(value, name);
        if (value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank.");
        }

        return value;
    }

    public static final class Builder {
        private final String id;
        private final String name;
        private final RegionType type;
        private final String ownerId;
        private final String worldName;
        private final RegionShape shape;
        private Integer centerX;
        private Integer centerZ;
        private Integer radius;
        private Integer pos1X;
        private Integer pos1Y;
        private Integer pos1Z;
        private Integer pos2X;
        private Integer pos2Y;
        private Integer pos2Z;
        private int minY;
        private int maxY;
        private long createdAt = System.currentTimeMillis();
        private boolean enabled = true;
        private long fuelExpiresAt;
        private long fuelEmptySince;
        private long lastFuelDrainAt;
        private int upgradeLevel;
        private VisualizationMode visualizationMode = VisualizationMode.ALL;

        private Builder(String id, String name, RegionType type, String ownerId, String worldName, RegionShape shape) {
            this.id = id;
            this.name = name;
            this.type = type;
            this.ownerId = ownerId;
            this.worldName = worldName;
            this.shape = shape;
        }

        public Builder radius(int centerX, int centerZ, int radius, int minY, int maxY) {
            this.centerX = centerX;
            this.centerZ = centerZ;
            this.radius = radius;
            this.minY = minY;
            this.maxY = maxY;
            return this;
        }

        public Builder cuboid(int pos1X, int pos1Y, int pos1Z, int pos2X, int pos2Y, int pos2Z) {
            this.pos1X = pos1X;
            this.pos1Y = pos1Y;
            this.pos1Z = pos1Z;
            this.pos2X = pos2X;
            this.pos2Y = pos2Y;
            this.pos2Z = pos2Z;
            this.minY = Math.min(pos1Y, pos2Y);
            this.maxY = Math.max(pos1Y, pos2Y);
            return this;
        }

        public Builder state(boolean enabled, long fuelExpiresAt, long fuelEmptySince, long lastFuelDrainAt,
                             int upgradeLevel, VisualizationMode visualizationMode, long createdAt) {
            this.enabled = enabled;
            this.fuelExpiresAt = fuelExpiresAt;
            this.fuelEmptySince = fuelEmptySince;
            this.lastFuelDrainAt = lastFuelDrainAt;
            this.upgradeLevel = upgradeLevel;
            this.visualizationMode = visualizationMode;
            this.createdAt = createdAt;
            return this;
        }

        public Region build() {
            return new Region(this);
        }
    }
}
