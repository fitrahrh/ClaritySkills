package com.harukiyu.clarityskills.config;

import com.harukiyu.clarityskills.ClaritySkills;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

public class ConfigManager {
    private final ClaritySkills plugin;

    private int maxLevel;
    private long baseExp;
    private double expExponent;
    private int pointsPerLevel;
    private int statMaxLevel;
    private int costPerStatPoint;

    private int tier1Threshold;
    private int tier2Threshold;

    private boolean enableBuiltinCombat;
    private boolean enableBuiltinElemental;
    private double attackDamagePerPoint;
    private double defenseReductionPerPoint;
    private double defenseReductionCap;
    private double magicDamageMultiplierPerPoint;

    private boolean playLevelUpSound;
    private String levelUpSound;
    private boolean playTierUnlockSound;
    private String tierUnlockSound;

    private boolean registerPauseScreen;

    public ConfigManager(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        plugin.reloadConfig();
        FileConfiguration config = plugin.getConfig();

        maxLevel = config.getInt("settings.max_level", 100);
        baseExp = config.getLong("settings.base_exp", 100);
        expExponent = config.getDouble("settings.exp_exponent", 1.5);
        pointsPerLevel = config.getInt("settings.points_per_level", 2);
        statMaxLevel = config.getInt("settings.stat_max_level", 100);
        costPerStatPoint = config.getInt("settings.cost_per_stat_point", 1);

        tier1Threshold = config.getInt("elements.tier_1_threshold", 25);
        tier2Threshold = config.getInt("elements.tier_2_threshold", 50);

        enableBuiltinCombat = config.getBoolean("mechanics.enable_builtin_combat_mechanics", true);
        enableBuiltinElemental = config.getBoolean("mechanics.enable_builtin_elemental_mechanics", true);
        attackDamagePerPoint = config.getDouble("mechanics.attack_damage_per_point", 0.15);
        defenseReductionPerPoint = config.getDouble("mechanics.defense_reduction_per_point", 0.005);
        defenseReductionCap = config.getDouble("mechanics.defense_reduction_cap", 0.75);
        magicDamageMultiplierPerPoint = config.getDouble("mechanics.magic_damage_multiplier_per_point", 0.01);

        playLevelUpSound = config.getBoolean("effects.play_level_up_sound", true);
        levelUpSound = config.getString("effects.level_up_sound", "ui.toast.challenge_complete");
        playTierUnlockSound = config.getBoolean("effects.play_tier_unlock_sound", true);
        tierUnlockSound = config.getString("effects.tier_unlock_sound", "ui.toast.challenge_complete");

        registerPauseScreen = config.getBoolean("gui.register_pause_screen", true);
    }

    public long calculateRequiredExp(int currentLevel) {
        if (currentLevel >= maxLevel) return Long.MAX_VALUE;
        return Math.max(1, Math.round(baseExp * Math.pow(currentLevel, expExponent)));
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public long getBaseExp() {
        return baseExp;
    }

    public double getExpExponent() {
        return expExponent;
    }

    public int getPointsPerLevel() {
        return pointsPerLevel;
    }

    public int getStatMaxLevel() {
        return statMaxLevel;
    }

    public int getCostPerStatPoint() {
        return costPerStatPoint;
    }

    public int getTier1Threshold() {
        return tier1Threshold;
    }

    public int getTier2Threshold() {
        return tier2Threshold;
    }

    public boolean isEnableBuiltinCombat() {
        return enableBuiltinCombat;
    }

    public boolean isEnableBuiltinElemental() {
        return enableBuiltinElemental;
    }

    public double getAttackDamagePerPoint() {
        return attackDamagePerPoint;
    }

    public double getDefenseReductionPerPoint() {
        return defenseReductionPerPoint;
    }

    public double getDefenseReductionCap() {
        return defenseReductionCap;
    }

    public double getMagicDamageMultiplierPerPoint() {
        return magicDamageMultiplierPerPoint;
    }

    public boolean isPlayLevelUpSound() {
        return playLevelUpSound;
    }

    public String getLevelUpSound() {
        return levelUpSound;
    }

    public boolean isPlayTierUnlockSound() {
        return playTierUnlockSound;
    }

    public String getTierUnlockSound() {
        return tierUnlockSound;
    }

    public boolean isRegisterPauseScreen() {
        return registerPauseScreen;
    }
}
