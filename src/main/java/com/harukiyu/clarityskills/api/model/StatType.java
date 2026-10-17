package com.harukiyu.clarityskills.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public enum StatType {
    ATTACK("attack", "stat.attack.name", "stat.attack.desc", false, "#FF5555"),
    MAGIC("magic", "stat.magic.name", "stat.magic.desc", false, "#AA00AA"),
    DEFENSE("defense", "stat.defense.name", "stat.defense.desc", false, "#55FF55"),
    FIRE("fire", "stat.fire.name", "stat.fire.desc", true, "#FFAA00"),
    WIND("wind", "stat.wind.name", "stat.wind.desc", true, "#55FFFF"),
    WATER("water", "stat.water.name", "stat.water.desc", true, "#00AAFF"),
    EARTH("earth", "stat.earth.name", "stat.earth.desc", true, "#00AA00"),
    LIGHT("light", "stat.light.name", "stat.light.desc", true, "#FFFF55");

    private final String id;
    private final String nameKey;
    private final String descKey;
    private final boolean elemental;
    private final String colorHex;

    StatType(@NotNull String id, @NotNull String nameKey, @NotNull String descKey, boolean elemental, @NotNull String colorHex) {
        this.id = id;
        this.nameKey = nameKey;
        this.descKey = descKey;
        this.elemental = elemental;
        this.colorHex = colorHex;
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getNameKey() {
        return nameKey;
    }

    @NotNull
    public String getDescKey() {
        return descKey;
    }

    public boolean isElemental() {
        return elemental;
    }

    @NotNull
    public String getColorHex() {
        return colorHex;
    }

    @Nullable
    public static StatType fromId(@Nullable String id) {
        if (id == null) return null;
        for (StatType type : values()) {
            if (type.id.equalsIgnoreCase(id) || type.name().equalsIgnoreCase(id)) {
                return type;
            }
        }
        return null;
    }

    @NotNull
    public static List<StatType> getCoreStats() {
        return List.of(ATTACK, MAGIC, DEFENSE);
    }

    @NotNull
    public static List<StatType> getElementalStats() {
        return List.of(FIRE, WIND, WATER, EARTH, LIGHT);
    }
}
