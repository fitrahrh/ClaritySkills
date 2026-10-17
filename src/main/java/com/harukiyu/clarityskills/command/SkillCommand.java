package com.harukiyu.clarityskills.command;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import com.harukiyu.clarityskills.config.ConfigManager;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import com.harukiyu.clarityskills.hook.LanguageHook;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkillCommand implements CommandExecutor, TabCompleter {
    private final ClaritySkills plugin;

    public SkillCommand(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /skill menu.");
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("gui")) {
            plugin.getDialogManager().openMainMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("info")) {
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
            ConfigManager config = plugin.getConfigManager();
            long reqExp = config.calculateRequiredExp(data.getLevel());
            double boost = plugin.getExpBoostManager().getEffectiveMultiplier(player);

            LanguageHook.sendMessage(player, "command.skill.info",
                    Placeholder.unparsed("level", String.valueOf(data.getLevel())),
                    Placeholder.unparsed("exp", String.valueOf(data.getCurrentExp())),
                    Placeholder.unparsed("req_exp", String.valueOf(reqExp)),
                    Placeholder.unparsed("points", String.valueOf(data.getSkillPoints())),
                    Placeholder.unparsed("boost", String.format("%.1fx", boost)));
            return true;
        }

        if (args[0].equalsIgnoreCase("stats")) {
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
            ConfigManager config = plugin.getConfigManager();

            LanguageHook.sendMessage(player, "command.skill.stats_header");
            for (StatType stat : StatType.values()) {
                int level = data.getStat(stat);
                if (stat.isElemental()) {
                    ElementalTier tier = data.getElementalTier(stat, config.getTier1Threshold(), config.getTier2Threshold());
                    LanguageHook.sendMessage(player, "command.skill.stat_element_entry",
                            Placeholder.component("stat", LanguageHook.getComponent(stat.getNameKey(), player)),
                            Placeholder.unparsed("level", String.valueOf(level)),
                            Placeholder.component("tier", LanguageHook.getComponent(tier.getNameKey(), player)));
                } else {
                    LanguageHook.sendMessage(player, "command.skill.stat_core_entry",
                            Placeholder.component("stat", LanguageHook.getComponent(stat.getNameKey(), player)),
                            Placeholder.unparsed("level", String.valueOf(level)));
                }
            }
            return true;
        }

        LanguageHook.sendMessage(player, "command.skill.help");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = List.of("menu", "info", "stats", "help");
            return list.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }
        return List.of();
    }
}
