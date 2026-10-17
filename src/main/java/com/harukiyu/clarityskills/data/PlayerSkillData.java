package com.harukiyu.clarityskills.data;

import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import org.jetbrains.annotations.NotNull;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class PlayerSkillData {
    private final UUID uuid;
    private int level = 1;
    private long currentExp = 0;
    private long totalExp = 0;
    private int skillPoints = 0;
    private final Map<StatType, Integer> stats = new EnumMap<>(StatType.class);
    private ExpBoost personalBoost = new ExpBoost(1.0, 0);
    private volatile boolean dirty = false;

    public PlayerSkillData(@NotNull UUID uuid) {
        this.uuid = uuid;
        for (StatType type : StatType.values()) {
            stats.put(type, 0);
        }
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(1, level);
        this.dirty = true;
    }

    public long getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(long currentExp) {
        this.currentExp = Math.max(0, currentExp);
        this.dirty = true;
    }

    public long getTotalExp() {
        return totalExp;
    }

    public void setTotalExp(long totalExp) {
        this.totalExp = Math.max(0, totalExp);
        this.dirty = true;
    }

    public int getSkillPoints() {
        return skillPoints;
    }

    public void setSkillPoints(int skillPoints) {
        this.skillPoints = Math.max(0, skillPoints);
        this.dirty = true;
    }

    public void addSkillPoints(int points) {
        this.skillPoints = Math.max(0, this.skillPoints + points);
        this.dirty = true;
    }

    public boolean removeSkillPoints(int points) {
        if (this.skillPoints >= points) {
            this.skillPoints -= points;
            this.dirty = true;
            return true;
        }
        return false;
    }

    public int getStat(@NotNull StatType type) {
        return stats.getOrDefault(type, 0);
    }

    public void setStat(@NotNull StatType type, int value) {
        stats.put(type, Math.max(0, value));
        this.dirty = true;
    }

    public void addStat(@NotNull StatType type, int amount) {
        setStat(type, getStat(type) + amount);
    }

    @NotNull
    public Map<StatType, Integer> getAllStats() {
        return new EnumMap<>(stats);
    }

    @NotNull
    public ElementalTier getElementalTier(@NotNull StatType element, int tier1Threshold, int tier2Threshold) {
        return ElementalTier.getTier(element, getStat(element), tier1Threshold, tier2Threshold);
    }

    @NotNull
    public ExpBoost getPersonalBoost() {
        return personalBoost;
    }

    public void setPersonalBoost(@NotNull ExpBoost boost) {
        this.personalBoost = boost;
        this.dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }
}
