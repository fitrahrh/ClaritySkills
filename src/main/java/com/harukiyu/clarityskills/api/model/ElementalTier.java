package com.harukiyu.clarityskills.api.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ElementalTier {
    NONE(null, 0, "tier.none.name", "tier.none.desc", "#AAAAAA"),

    // Fire Tiers
    FIRE_BLAZE(StatType.FIRE, 1, "tier.fire.blaze.name", "tier.fire.blaze.desc", "#FFAA00"),
    FIRE_INFERNO(StatType.FIRE, 2, "tier.fire.inferno.name", "tier.fire.inferno.desc", "#FF5500"),

    // Wind Tiers
    WIND_BADAI(StatType.WIND, 1, "tier.wind.badai.name", "tier.wind.badai.desc", "#55FFFF"),
    WIND_STORM(StatType.WIND, 2, "tier.wind.storm.name", "tier.wind.storm.desc", "#00AAAA"),

    // Water Tiers
    WATER_GELOMBANG(StatType.WATER, 1, "tier.water.gelombang.name", "tier.water.gelombang.desc", "#55AAFF"),
    WATER_ICE(StatType.WATER, 2, "tier.water.ice.name", "tier.water.ice.desc", "#00FFFF"),

    // Earth Tiers
    EARTH_MEGALIT(StatType.EARTH, 1, "tier.earth.megalit.name", "tier.earth.megalit.desc", "#00AA00"),
    EARTH_BUMI(StatType.EARTH, 2, "tier.earth.bumi.name", "tier.earth.bumi.desc", "#006600"),

    // Light Tiers
    LIGHT_SUAR(StatType.LIGHT, 1, "tier.light.suar.name", "tier.light.suar.desc", "#FFFF55"),
    LIGHT_FOTON(StatType.LIGHT, 2, "tier.light.foton.name", "tier.light.foton.desc", "#FFFFFF");

    private final StatType element;
    private final int tierNumber;
    private final String nameKey;
    private final String descKey;
    private final String colorHex;

    ElementalTier(@Nullable StatType element, int tierNumber, @NotNull String nameKey, @NotNull String descKey, @NotNull String colorHex) {
        this.element = element;
        this.tierNumber = tierNumber;
        this.nameKey = nameKey;
        this.descKey = descKey;
        this.colorHex = colorHex;
    }

    @Nullable
    public StatType getElement() {
        return element;
    }

    public int getTierNumber() {
        return tierNumber;
    }

    @NotNull
    public String getNameKey() {
        return nameKey;
    }

    @NotNull
    public String getDescKey() {
        return descKey;
    }

    @NotNull
    public String getColorHex() {
        return colorHex;
    }

    @NotNull
    public static ElementalTier getTier(@NotNull StatType element, int level, int tier1Threshold, int tier2Threshold) {
        if (!element.isElemental()) return NONE;

        if (level >= tier2Threshold) {
            return switch (element) {
                case FIRE -> FIRE_INFERNO;
                case WIND -> WIND_STORM;
                case WATER -> WATER_ICE;
                case EARTH -> EARTH_BUMI;
                case LIGHT -> LIGHT_FOTON;
                default -> NONE;
            };
        } else if (level >= tier1Threshold) {
            return switch (element) {
                case FIRE -> FIRE_BLAZE;
                case WIND -> WIND_BADAI;
                case WATER -> WATER_GELOMBANG;
                case EARTH -> EARTH_MEGALIT;
                case LIGHT -> LIGHT_SUAR;
                default -> NONE;
            };
        }
        return NONE;
    }

    @Nullable
    public static ElementalTier getTier1(@NotNull StatType element) {
        return switch (element) {
            case FIRE -> FIRE_BLAZE;
            case WIND -> WIND_BADAI;
            case WATER -> WATER_GELOMBANG;
            case EARTH -> EARTH_MEGALIT;
            case LIGHT -> LIGHT_SUAR;
            default -> null;
        };
    }

    @Nullable
    public static ElementalTier getTier2(@NotNull StatType element) {
        return switch (element) {
            case FIRE -> FIRE_INFERNO;
            case WIND -> WIND_STORM;
            case WATER -> WATER_ICE;
            case EARTH -> EARTH_BUMI;
            case LIGHT -> LIGHT_FOTON;
            default -> null;
        };
    }
}
