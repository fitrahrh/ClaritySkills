package com.harukiyu.clarityskills.command;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.data.ExpBoost;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import com.harukiyu.clarityskills.hook.LanguageHook;
import com.harukiyu.clarityskills.manager.CommandHelper;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkillAdminCommand implements CommandExecutor, TabCompleter {
    private final ClaritySkills plugin;

    public SkillAdminCommand(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Player playerSender = sender instanceof Player p ? p : null;

        if (!sender.hasPermission("clarityskills.admin")) {
            if (playerSender != null) {
                LanguageHook.sendMessage(playerSender, "general.no_permission");
            } else {
                sender.sendMessage("You do not have permission to use this command.");
            }
            return true;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendAdminHelp(sender, playerSender);
            return true;
        }

        String sub = args[0].toLowerCase();

        // 1. RELOAD
        if (sub.equals("reload")) {
            plugin.getConfigManager().reload();
            LanguageHook.init(plugin, "clarityskills");
            if (playerSender != null) {
                LanguageHook.sendMessage(playerSender, "command.admin.reloaded");
            } else {
                sender.sendMessage("Configuration and languages reloaded.");
            }
            return true;
        }

        // 2. EXP COMMANDS: /skilladmin exp <give|take|set|reset> <player> <amount> [boost]
        if (sub.equals("exp")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /skilladmin exp <give|take|set|reset> <player> [amount] [applyBoost]");
                return true;
            }
            String action = args[1].toLowerCase();
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                return true;
            }
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);

            switch (action) {
                case "give" -> {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /skilladmin exp give <player> <amount> [applyBoost]");
                        return true;
                    }
                    long amount = parseLong(args[3]);
                    boolean applyBoost = args.length < 5 || Boolean.parseBoolean(args[4]);
                    plugin.getSkillManager().giveExp(target, amount, applyBoost);
                    sendMsg(sender, playerSender, "command.admin.exp_given",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("boosted", String.valueOf(applyBoost)));
                    return true;
                }
                case "take" -> {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /skilladmin exp take <player> <amount>");
                        return true;
                    }
                    long amount = parseLong(args[3]);
                    data.setCurrentExp(Math.max(0, data.getCurrentExp() - amount));
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.exp_taken",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return true;
                }
                case "set" -> {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /skilladmin exp set <player> <amount>");
                        return true;
                    }
                    long amount = parseLong(args[3]);
                    data.setCurrentExp(amount);
                    plugin.getSkillManager().checkLevelUp(target, data);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.exp_set",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return true;
                }
                case "reset" -> {
                    data.setCurrentExp(0);
                    data.setTotalExp(0);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.exp_reset", Placeholder.unparsed("player", target.getName()));
                    return true;
                }
                default -> {}
            }
        }

        // 3. POINT COMMANDS: /skilladmin point <give|take|set|reset> <player> <amount>
        if (sub.equals("point") || sub.equals("points")) {
            if (args.length < 3) {
                sender.sendMessage("Usage: /skilladmin point <give|take|set|reset> <player> [amount]");
                return true;
            }
            String action = args[1].toLowerCase();
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                return true;
            }
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);

            switch (action) {
                case "give" -> {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /skilladmin point give <player> <amount>");
                        return true;
                    }
                    int amount = parseInt(args[3]);
                    data.addSkillPoints(amount);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_given",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    LanguageHook.sendMessage(target, "point.received", Placeholder.unparsed("amount", String.valueOf(amount)));
                    return true;
                }
                case "take" -> {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /skilladmin point take <player> <amount>");
                        return true;
                    }
                    int amount = parseInt(args[3]);
                    data.removeSkillPoints(amount);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_taken",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return true;
                }
                case "set" -> {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /skilladmin point set <player> <amount>");
                        return true;
                    }
                    int amount = parseInt(args[3]);
                    data.setSkillPoints(amount);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_set",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return true;
                }
                case "reset" -> {
                    data.setSkillPoints(0);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_reset", Placeholder.unparsed("player", target.getName()));
                    return true;
                }
                default -> {}
            }
        }

        // 4. BOOST COMMANDS: /skilladmin boost <give|take|global|clear|list> ...
        if (sub.equals("boost") || sub.equals("buff")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /skilladmin boost <give|take|global|clear|list> ...");
                return true;
            }
            String action = args[1].toLowerCase();

            switch (action) {
                case "give" -> {
                    if (args.length < 5) {
                        sender.sendMessage("Usage: /skilladmin boost give <player> <multiplier (e.g. 2.0)> <duration (e.g. 30m, 1h, perm)>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                        return true;
                    }
                    double mult = parseDouble(args[3]);
                    long duration = CommandHelper.parseDurationMillis(args[4]);
                    plugin.getExpBoostManager().setPlayerBoost(target, mult, duration);
                    sendMsg(sender, playerSender, "command.admin.boost_given",
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                            Placeholder.unparsed("duration", args[4]));
                    LanguageHook.sendMessage(target, "boost.received",
                            Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                            Placeholder.unparsed("duration", args[4]));
                    return true;
                }
                case "take", "remove" -> {
                    if (args.length < 3) {
                        sender.sendMessage("Usage: /skilladmin boost take <player>");
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                        return true;
                    }
                    plugin.getExpBoostManager().clearPlayerBoost(target);
                    sendMsg(sender, playerSender, "command.admin.boost_cleared_player", Placeholder.unparsed("player", target.getName()));
                    return true;
                }
                case "global" -> {
                    if (args.length < 4) {
                        sender.sendMessage("Usage: /skilladmin boost global <multiplier (e.g. 2.0)> <duration (e.g. 1h, perm)>");
                        return true;
                    }
                    double mult = parseDouble(args[2]);
                    long duration = CommandHelper.parseDurationMillis(args[3]);
                    plugin.getExpBoostManager().setGlobalBoost(mult, duration);
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        LanguageHook.sendMessage(p, "boost.global_broadcast",
                                Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                                Placeholder.unparsed("duration", args[3]));
                    }
                    sendMsg(sender, playerSender, "command.admin.boost_global_set",
                            Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                            Placeholder.unparsed("duration", args[3]));
                    return true;
                }
                case "clear" -> {
                    if (args.length >= 3 && args[2].equalsIgnoreCase("global")) {
                        plugin.getExpBoostManager().clearGlobalBoost();
                        sendMsg(sender, playerSender, "command.admin.boost_global_cleared");
                        return true;
                    }
                    if (args.length >= 3) {
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target != null) {
                            plugin.getExpBoostManager().clearPlayerBoost(target);
                            sendMsg(sender, playerSender, "command.admin.boost_cleared_player", Placeholder.unparsed("player", target.getName()));
                            return true;
                        }
                    }
                    sender.sendMessage("Usage: /skilladmin boost clear <global|player>");
                    return true;
                }
                case "list" -> {
                    ExpBoost global = plugin.getExpBoostManager().getGlobalBoost();
                    sender.sendMessage("--- EXP Boost Status ---");
                    sender.sendMessage("Global Boost: " + (global.isActive() ? String.format("%.1fx (Remaining: %s)", global.getMultiplier(), global.getFormattedRemaining()) : "None"));
                    if (args.length >= 3) {
                        Player target = Bukkit.getPlayer(args[2]);
                        if (target != null) {
                            PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);
                            ExpBoost pb = data.getPersonalBoost();
                            sender.sendMessage(target.getName() + " Personal Boost: " + (pb.isActive() ? String.format("%.1fx (Remaining: %s)", pb.getMultiplier(), pb.getFormattedRemaining()) : "None"));
                            sender.sendMessage("Total Effective Multiplier: " + String.format("%.1fx", plugin.getExpBoostManager().getEffectiveMultiplier(target)));
                        }
                    }
                    return true;
                }
                default -> {}
            }
        }

        // 5. LEVEL COMMAND: /skilladmin level set <player> <level>
        if (sub.equals("level")) {
            if (args.length < 4 || !args[1].equalsIgnoreCase("set")) {
                sender.sendMessage("Usage: /skilladmin level set <player> <level>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                return true;
            }
            int lvl = parseInt(args[3]);
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);
            data.setLevel(lvl);
            data.setCurrentExp(0);
            plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
            sendMsg(sender, playerSender, "command.admin.level_set",
                    Placeholder.unparsed("player", target.getName()),
                    Placeholder.unparsed("level", String.valueOf(lvl)));
            return true;
        }

        // 6. RESET COMMAND: /skilladmin reset <player> [all|stats|points]
        if (sub.equals("reset")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /skilladmin reset <player> [all|stats|points]");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[1]));
                return true;
            }
            String mode = args.length >= 3 ? args[2].toLowerCase() : "all";
            boolean resetPoints = mode.equals("all") || mode.equals("points");
            boolean resetStats = mode.equals("all") || mode.equals("stats");

            plugin.getSkillManager().resetPlayer(target, resetPoints, resetStats);
            sendMsg(sender, playerSender, "command.admin.player_reset",
                    Placeholder.unparsed("player", target.getName()),
                    Placeholder.unparsed("mode", mode));
            return true;
        }

        sendAdminHelp(sender, playerSender);
        return true;
    }

    private void sendAdminHelp(CommandSender sender, @Nullable Player playerSender) {
        if (playerSender != null) {
            LanguageHook.sendMessage(playerSender, "command.admin.help");
        } else {
            sender.sendMessage("=== ClaritySkills Admin Commands ===");
            sender.sendMessage("/skilladmin exp give <player> <amount> [boost: true|false]");
            sender.sendMessage("/skilladmin exp take <player> <amount>");
            sender.sendMessage("/skilladmin exp set <player> <amount>");
            sender.sendMessage("/skilladmin exp reset <player>");
            sender.sendMessage("/skilladmin point give <player> <amount>");
            sender.sendMessage("/skilladmin point take <player> <amount>");
            sender.sendMessage("/skilladmin point set <player> <amount>");
            sender.sendMessage("/skilladmin point reset <player>");
            sender.sendMessage("/skilladmin boost give <player> <multiplier> <duration (e.g. 30m, 1h, perm)>");
            sender.sendMessage("/skilladmin boost global <multiplier> <duration>");
            sender.sendMessage("/skilladmin boost clear <global|player>");
            sender.sendMessage("/skilladmin boost list [player]");
            sender.sendMessage("/skilladmin level set <player> <level>");
            sender.sendMessage("/skilladmin reset <player> [all|stats|points]");
            sender.sendMessage("/skilladmin reload");
        }
    }

    private void sendMsg(CommandSender sender, @Nullable Player playerSender, String key, net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... resolvers) {
        if (playerSender != null) {
            LanguageHook.sendMessage(playerSender, key, resolvers);
        } else {
            sender.sendMessage(LanguageHook.getComponent(key, null, resolvers));
        }
    }

    private int parseInt(String str) {
        try { return Integer.parseInt(str); } catch (NumberFormatException e) { return 1; }
    }

    private long parseLong(String str) {
        try { return Long.parseLong(str); } catch (NumberFormatException e) { return 1L; }
    }

    private double parseDouble(String str) {
        try { return Double.parseDouble(str); } catch (NumberFormatException e) { return 1.0; }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("clarityskills.admin")) return List.of();

        if (args.length == 1) {
            List<String> subs = List.of("exp", "point", "boost", "level", "reset", "reload", "help");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase())).toList();
        }

        String sub = args[0].toLowerCase();
        if (sub.equals("exp")) {
            return switch (args.length) {
                case 2 -> List.of("give", "take", "set", "reset").stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
                case 3 -> null; // players
                case 4 -> List.of("10", "50", "100", "500", "1000");
                case 5 -> List.of("true", "false");
                default -> List.of();
            };
        }

        if (sub.equals("point") || sub.equals("points")) {
            return switch (args.length) {
                case 2 -> List.of("give", "take", "set", "reset").stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
                case 3 -> null; // players
                case 4 -> List.of("1", "5", "10", "50");
                default -> List.of();
            };
        }

        if (sub.equals("boost") || sub.equals("buff")) {
            if (args.length == 2) {
                return List.of("give", "take", "global", "clear", "list").stream().filter(s -> s.startsWith(args[1].toLowerCase())).toList();
            } else if (args.length == 3) {
                if (args[1].equalsIgnoreCase("global")) {
                    return List.of("1.5", "2.0", "3.0");
                }
                return null; // players
            } else if (args.length == 4) {
                if (args[1].equalsIgnoreCase("give")) {
                    return List.of("1.5", "2.0", "3.0");
                } else if (args[1].equalsIgnoreCase("global")) {
                    return List.of("30m", "1h", "2h", "1d", "perm");
                }
            } else if (args.length == 5 && args[1].equalsIgnoreCase("give")) {
                return List.of("15m", "30m", "1h", "1d", "perm");
            }
        }

        if (sub.equals("level")) {
            if (args.length == 2) return List.of("set");
            if (args.length == 3) return null; // player
            if (args.length == 4) return List.of("1", "10", "25", "50", "100");
        }

        if (sub.equals("reset")) {
            if (args.length == 2) return null; // player
            if (args.length == 3) return List.of("all", "stats", "points");
        }

        return List.of();
    }
}
