package com.harukiyu.clarityskills.api.event;

import com.harukiyu.clarityskills.api.model.StatType;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerStatAllocateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final StatType statType;
    private final int pointsSpent;
    private final int levelsAdded;
    private final int oldLevel;
    private final int newLevel;
    private boolean cancelled = false;

    public PlayerStatAllocateEvent(@NotNull Player player, @NotNull StatType statType, int pointsSpent, int levelsAdded, int oldLevel, int newLevel) {
        this.player = player;
        this.statType = statType;
        this.pointsSpent = pointsSpent;
        this.levelsAdded = levelsAdded;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public StatType getStatType() {
        return statType;
    }

    public int getPointsSpent() {
        return pointsSpent;
    }

    public int getLevelsAdded() {
        return levelsAdded;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
