package com.harukiyu.clarityskills.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerExpBoostEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final double multiplier;
    private final long durationMillis;
    private final boolean global;

    public PlayerExpBoostEvent(@Nullable Player player, double multiplier, long durationMillis, boolean global) {
        this.player = player;
        this.multiplier = multiplier;
        this.durationMillis = durationMillis;
        this.global = global;
    }

    @Nullable
    public Player getPlayer() {
        return player;
    }

    public double getMultiplier() {
        return multiplier;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public boolean isGlobal() {
        return global;
    }

    public boolean isPermanent() {
        return durationMillis <= 0;
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
