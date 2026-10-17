package com.harukiyu.clarityskills.manager;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.event.PlayerElementalTierUnlockEvent;
import com.harukiyu.clarityskills.api.event.PlayerExpGainEvent;
import com.harukiyu.clarityskills.api.event.PlayerSkillLevelUpEvent;
import com.harukiyu.clarityskills.api.event.PlayerStatAllocateEvent;
import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import com.harukiyu.clarityskills.config.ConfigManager;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import com.harukiyu.clarityskills.hook.LanguageHook;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class SkillManager {
    private final ClaritySkills plugin;

    public SkillManager(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    public void giveExp(@NotNull Player player, long baseExp, boolean applyBoost) {
        if (baseExp <= 0) return;

        ConfigManager config = plugin.getConfigManager();
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);

        if (data.getLevel() >= config.getMaxLevel()) {
            LanguageHook.sendMessage(player, "level.max_reached");
            return;
        }

        double multiplier = applyBoost ? plugin.getExpBoostManager().getEffectiveMultiplier(player) : 1.0;
        long finalExp = applyBoost ? Math.max(1, Math.round(baseExp * multiplier)) : baseExp;

        PlayerExpGainEvent expEvent = new PlayerExpGainEvent(player, baseExp, multiplier, finalExp);
        Bukkit.getPluginManager().callEvent(expEvent);
        if (expEvent.isCancelled()) return;

        finalExp = expEvent.getFinalExp();
        data.setTotalExp(data.getTotalExp() + finalExp);
        data.setCurrentExp(data.getCurrentExp() + finalExp);

        LanguageHook.sendMessage(player, "exp.received",
                Placeholder.unparsed("amount", String.valueOf(finalExp)),
                Placeholder.unparsed("multiplier", String.format("%.1fx", multiplier)));

        checkLevelUp(player, data);
        plugin.getStorageManager().savePlayerData(player.getUniqueId(), true);
    }

    public void checkLevelUp(@NotNull Player player, @NotNull PlayerSkillData data) {
        ConfigManager config = plugin.getConfigManager();
        int maxLevel = config.getMaxLevel();

        boolean leveledUp = false;

        while (data.getLevel() < maxLevel) {
            long reqExp = config.calculateRequiredExp(data.getLevel());
            if (data.getCurrentExp() < reqExp) {
                break;
            }

            data.setCurrentExp(data.getCurrentExp() - reqExp);
            data.setLevel(data.getLevel() + 1);

            int pointsGained = config.getPointsPerLevel();
            PlayerSkillLevelUpEvent event = new PlayerSkillLevelUpEvent(player, data.getLevel() - 1, data.getLevel(), pointsGained);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                data.addSkillPoints(event.getPointsGained());
            }
            leveledUp = true;
        }

        if (leveledUp) {
            onLevelUpSuccess(player, data.getLevel(), data.getSkillPoints());
        }
    }

    private void onLevelUpSuccess(@NotNull Player player, int newLevel, int totalPoints) {
        ConfigManager config = plugin.getConfigManager();

        if (config.isPlayLevelUpSound()) {
            try {
                String soundKey = config.getLevelUpSound().toLowerCase();
                net.kyori.adventure.key.Key key = soundKey.contains(":") ? net.kyori.adventure.key.Key.key(soundKey) : net.kyori.adventure.key.Key.key("minecraft", soundKey);
                player.playSound(net.kyori.adventure.sound.Sound.sound(key, net.kyori.adventure.sound.Sound.Source.PLAYER, 1.0f, 1.0f));
            } catch (Throwable ignored) {}
        }

        Title title = Title.title(
                LanguageHook.getComponent("level.up_title", player, Placeholder.unparsed("level", String.valueOf(newLevel))),
                LanguageHook.getComponent("level.up_subtitle", player, Placeholder.unparsed("points", String.valueOf(totalPoints))),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(500))
        );
        player.showTitle(title);

        LanguageHook.sendMessage(player, "level.up_message",
                Placeholder.unparsed("level", String.valueOf(newLevel)),
                Placeholder.unparsed("points", String.valueOf(totalPoints)));
    }

    public boolean allocateStat(@NotNull Player player, @NotNull StatType stat, int pointsToSpend) {
        if (pointsToSpend <= 0) return false;

        ConfigManager config = plugin.getConfigManager();
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);

        int costPerPoint = config.getCostPerStatPoint();
        int totalCost = pointsToSpend * costPerPoint;

        if (data.getSkillPoints() < totalCost) {
            LanguageHook.sendMessage(player, "stat.insufficient_points",
                    Placeholder.unparsed("required", String.valueOf(totalCost)),
                    Placeholder.unparsed("available", String.valueOf(data.getSkillPoints())));
            return false;
        }

        int currentStatLevel = data.getStat(stat);
        if (currentStatLevel >= config.getStatMaxLevel()) {
            LanguageHook.sendMessage(player, "stat.max_reached",
                    Placeholder.component("stat", LanguageHook.getComponent(stat.getNameKey(), player)));
            return false;
        }

        int actualIncrease = Math.min(pointsToSpend, config.getStatMaxLevel() - currentStatLevel);
        int actualCost = actualIncrease * costPerPoint;

        PlayerStatAllocateEvent event = new PlayerStatAllocateEvent(player, stat, actualCost, actualIncrease, currentStatLevel, currentStatLevel + actualIncrease);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) return false;

        ElementalTier oldTier = stat.isElemental() ? data.getElementalTier(stat, config.getTier1Threshold(), config.getTier2Threshold()) : ElementalTier.NONE;

        data.removeSkillPoints(actualCost);
        data.addStat(stat, actualIncrease);

        LanguageHook.sendMessage(player, "stat.allocated",
                Placeholder.component("stat", LanguageHook.getComponent(stat.getNameKey(), player)),
                Placeholder.unparsed("amount", String.valueOf(actualIncrease)),
                Placeholder.unparsed("level", String.valueOf(data.getStat(stat))));

        // Check for Elemental Tier Progression
        if (stat.isElemental()) {
            ElementalTier newTier = data.getElementalTier(stat, config.getTier1Threshold(), config.getTier2Threshold());
            if (newTier != oldTier && newTier.getTierNumber() > oldTier.getTierNumber()) {
                onElementalTierUnlock(player, stat, oldTier, newTier);
            }
        }

        plugin.getStorageManager().savePlayerData(player.getUniqueId(), true);
        return true;
    }

    private void onElementalTierUnlock(@NotNull Player player, @NotNull StatType element, @NotNull ElementalTier oldTier, @NotNull ElementalTier newTier) {
        ConfigManager config = plugin.getConfigManager();

        PlayerElementalTierUnlockEvent event = new PlayerElementalTierUnlockEvent(player, element, oldTier, newTier);
        Bukkit.getPluginManager().callEvent(event);

        if (config.isPlayTierUnlockSound()) {
            try {
                String soundKey = config.getTierUnlockSound().toLowerCase();
                net.kyori.adventure.key.Key key = soundKey.contains(":") ? net.kyori.adventure.key.Key.key(soundKey) : net.kyori.adventure.key.Key.key("minecraft", soundKey);
                player.playSound(net.kyori.adventure.sound.Sound.sound(key, net.kyori.adventure.sound.Sound.Source.PLAYER, 1.0f, 1.2f));
            } catch (Throwable ignored) {}
        }

        Title title = Title.title(
                LanguageHook.getComponent("tier.unlocked_title", player,
                        Placeholder.component("tier", LanguageHook.getComponent(newTier.getNameKey(), player))),
                LanguageHook.getComponent("tier.unlocked_subtitle", player,
                        Placeholder.component("element", LanguageHook.getComponent(element.getNameKey(), player))),
                Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3500), Duration.ofMillis(500))
        );
        player.showTitle(title);

        LanguageHook.sendMessage(player, "tier.unlocked_broadcast",
                Placeholder.unparsed("player", player.getName()),
                Placeholder.component("element", LanguageHook.getComponent(element.getNameKey(), player)),
                Placeholder.component("tier", LanguageHook.getComponent(newTier.getNameKey(), player)));
    }

    public void resetPlayer(@NotNull Player player, boolean resetPoints, boolean resetStats) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        if (resetStats) {
            for (StatType type : StatType.values()) {
                data.setStat(type, 0);
            }
        }
        if (resetPoints) {
            data.setSkillPoints(0);
        }
        plugin.getStorageManager().savePlayerData(player.getUniqueId(), true);
    }
}
