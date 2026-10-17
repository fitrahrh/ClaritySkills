package com.harukiyu.clarityskills.api.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerExpGainEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final long baseExp;
    private final double boostMultiplier;
    private long finalExp;
    private boolean cancelled = false;

    public PlayerExpGainEvent(@NotNull Player player, long baseExp, double boostMultiplier, long finalExp) {
        this.player = player;
        this.baseExp = baseExp;
        this.boostMultiplier = boostMultiplier;
        this.finalExp = finalExp;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    public long getBaseExp() {
        return baseExp;
    }

    public double getBoostMultiplier() {
        return boostMultiplier;
    }

    public long getFinalExp() {
        return finalExp;
    }

    public void setFinalExp(long finalExp) {
        this.finalExp = finalExp;
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
