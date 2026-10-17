package com.harukiyu.clarityskills.command;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import com.harukiyu.clarityskills.config.ConfigManager;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import com.harukiyu.clarityskills.hook.LanguageHook;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class SkillCommand implements BasicCommand {
    private final ClaritySkills plugin;

    public SkillCommand(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /skill menu.");
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("menu") || args[0].equalsIgnoreCase("gui")) {
            plugin.getDialogManager().openMainMenu(player);
            return;
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
            return;
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
            return;
        }

        LanguageHook.sendMessage(player, "command.skill.help");
    }

    @Override
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        if (args.length <= 1) {
            String prefix = args.length == 0 ? "" : args[0].toLowerCase();
            return List.of("menu", "info", "stats", "help").stream()
                    .filter(s -> s.startsWith(prefix))
                    .toList();
        }
        return List.of();
    }

    @Override
    public @Nullable String permission() {
        return "clarityskills.use";
    }
}
