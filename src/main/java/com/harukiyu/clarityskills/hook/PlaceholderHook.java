package com.harukiyu.clarityskills.hook;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import com.harukiyu.clarityskills.config.ConfigManager;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderHook extends PlaceholderExpansion {
    private final ClaritySkills plugin;

    public PlaceholderHook(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "clarityskills";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Harukiyu";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        ConfigManager config = plugin.getConfigManager();
        String param = params.toLowerCase();

        return switch (param) {
            case "level" -> String.valueOf(data.getLevel());
            case "exp" -> String.valueOf(data.getCurrentExp());
            case "total_exp" -> String.valueOf(data.getTotalExp());
            case "req_exp" -> String.valueOf(config.calculateRequiredExp(data.getLevel()));
            case "exp_percent" -> {
                long req = config.calculateRequiredExp(data.getLevel());
                yield req > 0 ? String.format("%.1f", ((double) data.getCurrentExp() / req) * 100.0) : "100.0";
            }
            case "points" -> String.valueOf(data.getSkillPoints());
            case "boost_multiplier" -> String.format("%.1fx", plugin.getExpBoostManager().getEffectiveMultiplier(player));
            case "boost_remaining" -> data.getPersonalBoost().getFormattedRemaining();
            default -> {
                if (param.startsWith("stat_")) {
                    String statName = param.substring(5);
                    StatType stat = StatType.fromId(statName);
                    if (stat != null) {
                        yield String.valueOf(data.getStat(stat));
                    }
                } else if (param.startsWith("tier_")) {
                    String elemName = param.substring(5);
                    StatType element = StatType.fromId(elemName);
                    if (element != null && element.isElemental()) {
                        ElementalTier tier = data.getElementalTier(element, config.getTier1Threshold(), config.getTier2Threshold());
                        String name = LanguageHook.getRawString(tier.getNameKey(), LanguageHook.getEffectiveLanguageCode(player));
                        yield name != null ? name : tier.name();
                    }
                }
                yield null;
            }
        };
    }
}
