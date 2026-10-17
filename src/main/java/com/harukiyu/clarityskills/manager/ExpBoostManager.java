package com.harukiyu.clarityskills.manager;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.event.PlayerExpBoostEvent;
import com.harukiyu.clarityskills.data.ExpBoost;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ExpBoostManager {
    private final ClaritySkills plugin;
    private ExpBoost globalBoost = new ExpBoost(1.0, 0);

    public ExpBoostManager(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    public void setGlobalBoost(double multiplier, long durationMillis) {
        this.globalBoost = new ExpBoost(multiplier, durationMillis);
        Bukkit.getPluginManager().callEvent(new PlayerExpBoostEvent(null, multiplier, durationMillis, true));
    }

    public void clearGlobalBoost() {
        this.globalBoost = new ExpBoost(1.0, 0);
    }

    public boolean isGlobalBoostActive() {
        return globalBoost.isActive();
    }

    public double getGlobalMultiplier() {
        return globalBoost.getMultiplier();
    }

    @NotNull
    public ExpBoost getGlobalBoost() {
        return globalBoost;
    }

    public void setPlayerBoost(@NotNull Player player, double multiplier, long durationMillis) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        data.setPersonalBoost(new ExpBoost(multiplier, durationMillis));
        Bukkit.getPluginManager().callEvent(new PlayerExpBoostEvent(player, multiplier, durationMillis, false));
    }

    public void clearPlayerBoost(@NotNull Player player) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        data.setPersonalBoost(new ExpBoost(1.0, 0));
    }

    public double getEffectiveMultiplier(@NotNull Player player) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        double personal = data.getPersonalBoost().getMultiplier();
        double global = getGlobalMultiplier();

        return personal * global;
    }

    public long calculateBoostedExp(@NotNull Player player, long baseExp) {
        double multiplier = getEffectiveMultiplier(player);
        return Math.max(1, Math.round(baseExp * multiplier));
    }
}
