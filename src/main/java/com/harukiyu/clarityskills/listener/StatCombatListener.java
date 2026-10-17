package com.harukiyu.clarityskills.listener;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import com.harukiyu.clarityskills.config.ConfigManager;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class StatCombatListener implements Listener {
    private final ClaritySkills plugin;

    public StatCombatListener(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        ConfigManager config = plugin.getConfigManager();

        // 1. Attacker is Player
        if (event.getDamager() instanceof Player attacker && event.getEntity() instanceof LivingEntity victim) {
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(attacker);

            if (config.isEnableBuiltinCombat()) {
                // Physical Attack bonus
                int atkLevel = data.getStat(StatType.ATTACK);
                if (atkLevel > 0) {
                    double bonus = atkLevel * config.getAttackDamagePerPoint();
                    event.setDamage(event.getDamage() + bonus);
                }

                // Magic Power bonus
                if (event.getCause() == EntityDamageEvent.DamageCause.MAGIC) {
                    int matkLevel = data.getStat(StatType.MAGIC);
                    if (matkLevel > 0) {
                        double magicBonus = 1.0 + (matkLevel * config.getMagicDamageMultiplierPerPoint());
                        event.setDamage(event.getDamage() * magicBonus);
                    }
                }
            }

            if (config.isEnableBuiltinElemental()) {
                // Fire Tier Effects
                ElementalTier fireTier = data.getElementalTier(StatType.FIRE, config.getTier1Threshold(), config.getTier2Threshold());
                if (fireTier == ElementalTier.FIRE_BLAZE) {
                    victim.setFireTicks(Math.max(victim.getFireTicks(), 60)); // 3 seconds
                } else if (fireTier == ElementalTier.FIRE_INFERNO) {
                    victim.setFireTicks(Math.max(victim.getFireTicks(), 140)); // 7 seconds
                    event.setDamage(event.getDamage() + 3.0); // +3 flat inferno bonus
                }

                // Water Tier Effects
                ElementalTier waterTier = data.getElementalTier(StatType.WATER, config.getTier1Threshold(), config.getTier2Threshold());
                if (waterTier == ElementalTier.WATER_ICE) {
                    victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1));
                    victim.setFreezeTicks(Math.max(victim.getFreezeTicks(), 100));
                }

                // Light Tier Effects
                ElementalTier lightTier = data.getElementalTier(StatType.LIGHT, config.getTier1Threshold(), config.getTier2Threshold());
                if (lightTier == ElementalTier.LIGHT_SUAR || lightTier == ElementalTier.LIGHT_FOTON) {
                    if (victim instanceof Monster) {
                        // Holy damage bonus against monsters
                        event.setDamage(event.getDamage() * (lightTier == ElementalTier.LIGHT_FOTON ? 1.5 : 1.25));
                    }
                    if (lightTier == ElementalTier.LIGHT_FOTON) {
                        // Photon Tier heals player slightly on hit
                        double currentHealth = attacker.getHealth();
                        var maxHealthAttr = attacker.getAttribute(Attribute.MAX_HEALTH);
                        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
                        attacker.setHealth(Math.min(maxHealth, currentHealth + 1.0));
                    }
                }
            }
        }

        // 2. Victim is Player (Defense & Earth mitigation)
        if (event.getEntity() instanceof Player victim) {
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(victim);

            if (config.isEnableBuiltinCombat()) {
                int defLevel = data.getStat(StatType.DEFENSE);
                if (defLevel > 0) {
                    double reduction = Math.min(config.getDefenseReductionCap(), defLevel * config.getDefenseReductionPerPoint());
                    event.setDamage(event.getDamage() * (1.0 - reduction));
                }
            }

            if (config.isEnableBuiltinElemental()) {
                ElementalTier earthTier = data.getElementalTier(StatType.EARTH, config.getTier1Threshold(), config.getTier2Threshold());
                if (earthTier == ElementalTier.EARTH_BUMI) {
                    if (victim.getHealth() < 6.0) { // Below 3 hearts
                        victim.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 80, 1));
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (!plugin.getConfigManager().isEnableBuiltinElemental()) return;

        if (event.getEntity() instanceof Player player && event.getCause() == EntityDamageEvent.DamageCause.FALL) {
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
            ConfigManager config = plugin.getConfigManager();
            ElementalTier windTier = data.getElementalTier(StatType.WIND, config.getTier1Threshold(), config.getTier2Threshold());

            if (windTier == ElementalTier.WIND_BADAI) {
                event.setDamage(event.getDamage() * 0.5); // 50% fall damage reduction
            } else if (windTier == ElementalTier.WIND_STORM) {
                event.setDamage(0); // Feather falling immunity
                event.setCancelled(true);
            }
        }
    }
}
