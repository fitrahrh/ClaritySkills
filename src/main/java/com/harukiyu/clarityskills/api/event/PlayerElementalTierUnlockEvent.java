package com.harukiyu.clarityskills.api.event;

import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class PlayerElementalTierUnlockEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();

    private final Player player;
    private final StatType element;
    private final ElementalTier oldTier;
    private final ElementalTier newTier;

    public PlayerElementalTierUnlockEvent(@NotNull Player player, @NotNull StatType element, @NotNull ElementalTier oldTier, @NotNull ElementalTier newTier) {
        this.player = player;
        this.element = element;
        this.oldTier = oldTier;
        this.newTier = newTier;
    }

    @NotNull
    public Player getPlayer() {
        return player;
    }

    @NotNull
    public StatType getElement() {
        return element;
    }

    @NotNull
    public ElementalTier getOldTier() {
        return oldTier;
    }

    @NotNull
    public ElementalTier getNewTier() {
        return newTier;
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
