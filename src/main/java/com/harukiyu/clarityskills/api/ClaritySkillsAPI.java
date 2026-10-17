package com.harukiyu.clarityskills.api;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public final class ClaritySkillsAPI {
    private static ClaritySkills plugin;

    private ClaritySkillsAPI() {}

    public static void init(@NotNull ClaritySkills instance) {
        plugin = instance;
    }

    @NotNull
    public static PlayerSkillData getPlayerData(@NotNull UUID uuid) {
        return plugin.getStorageManager().getPlayerData(uuid);
    }

    @NotNull
    public static PlayerSkillData getPlayerData(@NotNull Player player) {
        return getPlayerData(player.getUniqueId());
    }

    public static int getLevel(@NotNull UUID uuid) {
        return getPlayerData(uuid).getLevel();
    }

    public static int getSkillPoints(@NotNull UUID uuid) {
        return getPlayerData(uuid).getSkillPoints();
    }

    public static int getStat(@NotNull UUID uuid, @NotNull StatType stat) {
        return getPlayerData(uuid).getStat(stat);
    }

    @NotNull
    public static ElementalTier getElementalTier(@NotNull UUID uuid, @NotNull StatType element) {
        return getPlayerData(uuid).getElementalTier(
                element,
                plugin.getConfigManager().getTier1Threshold(),
                plugin.getConfigManager().getTier2Threshold()
        );
    }

    public static double getEffectiveExpBoost(@NotNull Player player) {
        return plugin.getExpBoostManager().getEffectiveMultiplier(player);
    }

    public static void giveExp(@NotNull Player player, long amount, boolean applyBoost) {
        plugin.getSkillManager().giveExp(player, amount, applyBoost);
    }

    public static void givePoints(@NotNull UUID uuid, int points) {
        PlayerSkillData data = getPlayerData(uuid);
        data.addSkillPoints(points);
        plugin.getStorageManager().savePlayerData(uuid, true);
    }

    public static boolean allocateStat(@NotNull Player player, @NotNull StatType stat, int points) {
        return plugin.getSkillManager().allocateStat(player, stat, points);
    }

    public static long getRequiredExpForLevel(int level) {
        return plugin.getConfigManager().calculateRequiredExp(level);
    }
}
