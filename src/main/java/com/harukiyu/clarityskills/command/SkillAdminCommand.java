package com.harukiyu.clarityskills.command;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.data.ExpBoost;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import com.harukiyu.clarityskills.hook.LanguageHook;
import com.harukiyu.clarityskills.manager.CommandHelper;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

public class SkillAdminCommand implements BasicCommand {
    private final ClaritySkills plugin;

    public SkillAdminCommand(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null) return;
        Player playerSender = sender instanceof Player p ? p : null;

        if (!sender.hasPermission("clarityskills.admin")) {
            if (playerSender != null) {
                LanguageHook.sendMessage(playerSender, "general.no_permission");
            } else {
                sender.sendMessage("You do not have permission to use this command.");
            }
            return;
        }

        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            sendAdminHelp(sender, playerSender);
            return;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        // 1. RELOAD
        if (sub.equals("reload")) {
            plugin.getConfigManager().reload();
            LanguageHook.init(plugin, "clarityskills");
            sendMsg(sender, playerSender, "command.admin.reloaded");
            return;
        }

        // 2. EXP COMMANDS: /skilladmin exp <give|take|set|reset> <player> <amount> [boost]
        if (sub.equals("exp")) {
            if (args.length < 3) {
                sendUsage(sender, playerSender, "/skilladmin exp <give|take|set|reset> <player> [amount] [applyBoost]");
                return;
            }
            String action = args[1].toLowerCase(Locale.ROOT);
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                return;
            }
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);

            switch (action) {
                case "give" -> {
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin exp give <player> <amount> [applyBoost]");
                        return;
                    }
                    long amount = parseLong(args[3]);
                    boolean applyBoost = args.length < 5 || Boolean.parseBoolean(args[4]);
                    plugin.getSkillManager().giveExp(target, amount, applyBoost);
                    sendMsg(sender, playerSender, "command.admin.exp_given",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("boosted", String.valueOf(applyBoost)));
                    return;
                }
                case "take" -> {
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin exp take <player> <amount>");
                        return;
                    }
                    long amount = parseLong(args[3]);
                    data.setCurrentExp(Math.max(0, data.getCurrentExp() - amount));
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.exp_taken",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return;
                }
                case "set" -> {
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin exp set <player> <amount>");
                        return;
                    }
                    long amount = parseLong(args[3]);
                    data.setCurrentExp(amount);
                    plugin.getSkillManager().checkLevelUp(target, data);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.exp_set",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return;
                }
                case "reset" -> {
                    data.setCurrentExp(0);
                    data.setTotalExp(0);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.exp_reset", Placeholder.unparsed("player", target.getName()));
                    return;
                }
                default -> {
                    sendUsage(sender, playerSender, "/skilladmin exp <give|take|set|reset> <player> [amount] [applyBoost]");
                    return;
                }
            }
        }

        // 3. POINT COMMANDS: /skilladmin point <give|take|set|reset> <player> <amount>
        if (sub.equals("point") || sub.equals("points")) {
            if (args.length < 3) {
                sendUsage(sender, playerSender, "/skilladmin point <give|take|set|reset> <player> [amount]");
                return;
            }
            String action = args[1].toLowerCase(Locale.ROOT);
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                return;
            }
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);

            switch (action) {
                case "give" -> {
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin point give <player> <amount>");
                        return;
                    }
                    int amount = parseInt(args[3]);
                    data.addSkillPoints(amount);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_given",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    LanguageHook.sendMessage(target, "point.received", Placeholder.unparsed("amount", String.valueOf(amount)));
                    return;
                }
                case "take" -> {
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin point take <player> <amount>");
                        return;
                    }
                    int amount = parseInt(args[3]);
                    data.removeSkillPoints(amount);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_taken",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return;
                }
                case "set" -> {
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin point set <player> <amount>");
                        return;
                    }
                    int amount = parseInt(args[3]);
                    data.setSkillPoints(amount);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_set",
                            Placeholder.unparsed("amount", String.valueOf(amount)),
                            Placeholder.unparsed("player", target.getName()));
                    return;
                }
                case "reset" -> {
                    data.setSkillPoints(0);
                    plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
                    sendMsg(sender, playerSender, "command.admin.point_reset", Placeholder.unparsed("player", target.getName()));
                    return;
                }
                default -> {
                    sendUsage(sender, playerSender, "/skilladmin point <give|take|set|reset> <player> [amount]");
                    return;
                }
            }
        }

        // 4. BOOST COMMANDS: /skilladmin boost <give|take|global|clear|list>
        if (sub.equals("boost") || sub.equals("buff")) {
            if (args.length < 2) {
                sendUsage(sender, playerSender, "/skilladmin boost <give|global|clear|list>");
                return;
            }
            String action = args[1].toLowerCase(Locale.ROOT);

            switch (action) {
                case "give" -> {
                    // /skilladmin boost give <player> <multiplier> [duration: 30m, 1h, perm]
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin boost give <player> <multiplier> [duration e.g. 30m, 1h, perm]");
                        return;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                        return;
                    }
                    double mult = parseDouble(args[3]);
                    long durationMillis = args.length >= 5 ? CommandHelper.parseDurationMillis(args[4]) : -1L;
                    plugin.getExpBoostManager().setPlayerBoost(target, mult, durationMillis);

                    String durStr = args.length >= 5 ? args[4] : "permanent";
                    sendMsg(sender, playerSender, "command.admin.boost_given",
                            Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                            Placeholder.unparsed("player", target.getName()),
                            Placeholder.unparsed("duration", durStr));
                    LanguageHook.sendMessage(target, "boost.received",
                            Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                            Placeholder.unparsed("duration", durStr));
                    return;
                }
                case "global" -> {
                    // /skilladmin boost global <multiplier> <duration>
                    if (args.length < 4) {
                        sendUsage(sender, playerSender, "/skilladmin boost global <multiplier> <duration e.g. 1h, 30m>");
                        return;
                    }
                    double mult = parseDouble(args[2]);
                    long durationMillis = CommandHelper.parseDurationMillis(args[3]);
                    plugin.getExpBoostManager().setGlobalBoost(mult, durationMillis);
                    sendMsg(sender, playerSender, "command.admin.boost_global_set",
                            Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                            Placeholder.unparsed("duration", args[3]));
                    Bukkit.broadcast(LanguageHook.getComponent("boost.global_broadcast", null,
                            Placeholder.unparsed("multiplier", String.format("%.1fx", mult)),
                            Placeholder.unparsed("duration", args[3])));
                    return;
                }
                case "clear" -> {
                    if (args.length < 3) {
                        sendUsage(sender, playerSender, "/skilladmin boost clear <global|player>");
                        return;
                    }
                    if (args[2].equalsIgnoreCase("global")) {
                        plugin.getExpBoostManager().clearGlobalBoost();
                        sendMsg(sender, playerSender, "command.admin.boost_global_cleared");
                        return;
                    }
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                        return;
                    }
                    plugin.getExpBoostManager().clearPlayerBoost(target);
                    sendMsg(sender, playerSender, "command.admin.boost_cleared_player", Placeholder.unparsed("player", target.getName()));
                    return;
                }
                case "list" -> {
                    Player target = args.length >= 3 ? Bukkit.getPlayer(args[2]) : playerSender;
                    if (target != null) {
                        PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);
                        ExpBoost pb = data.getPersonalBoost();
                        sendMsg(sender, playerSender, "command.admin.boost_list_player",
                                Placeholder.unparsed("player", target.getName()),
                                Placeholder.unparsed("multiplier", String.format("%.1fx", plugin.getExpBoostManager().getEffectiveMultiplier(target))),
                                Placeholder.unparsed("duration", pb.isActive() ? pb.getFormattedRemaining() : "None"));
                    }
                    ExpBoost gb = plugin.getExpBoostManager().getGlobalBoost();
                    sendMsg(sender, playerSender, "command.admin.boost_list_global",
                            Placeholder.unparsed("multiplier", String.format("%.1fx", gb.getMultiplier())),
                            Placeholder.unparsed("duration", gb.isActive() ? gb.getFormattedRemaining() : "None"));
                    return;
                }
                default -> {
                    sendUsage(sender, playerSender, "/skilladmin boost <give|global|clear|list>");
                    return;
                }
            }
        }

        // 5. LEVEL SET COMMAND: /skilladmin level set <player> <level>
        if (sub.equals("level")) {
            if (args.length < 4 || !args[1].equalsIgnoreCase("set")) {
                sendUsage(sender, playerSender, "/skilladmin level set <player> <level>");
                return;
            }
            Player target = Bukkit.getPlayer(args[2]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[2]));
                return;
            }
            int level = parseInt(args[3]);
            PlayerSkillData data = plugin.getStorageManager().getPlayerData(target);
            data.setLevel(Math.max(1, Math.min(level, plugin.getConfigManager().getMaxLevel())));
            plugin.getStorageManager().savePlayerData(target.getUniqueId(), true);
            sendMsg(sender, playerSender, "command.admin.level_set",
                    Placeholder.unparsed("level", String.valueOf(data.getLevel())),
                    Placeholder.unparsed("player", target.getName()));
            return;
        }

        // 6. RESET COMMAND: /skilladmin reset <player> [all|stats|points]
        if (sub.equals("reset")) {
            if (args.length < 2) {
                sendUsage(sender, playerSender, "/skilladmin reset <player> [all|stats|points]");
                return;
            }
            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sendMsg(sender, playerSender, "command.admin.player_not_found", Placeholder.unparsed("player", args[1]));
                return;
            }
            String mode = args.length >= 3 ? args[2].toLowerCase(Locale.ROOT) : "all";
            boolean resetPoints = mode.equals("all") || mode.equals("points");
            boolean resetStats = mode.equals("all") || mode.equals("stats");

            plugin.getSkillManager().resetPlayer(target, resetPoints, resetStats);
            sendMsg(sender, playerSender, "command.admin.player_reset",
                    Placeholder.unparsed("player", target.getName()),
                    Placeholder.unparsed("mode", mode));
            return;
        }

        sendAdminHelp(sender, playerSender);
    }

    private void sendAdminHelp(CommandSender sender, @Nullable Player playerSender) {
        if (playerSender != null) {
            LanguageHook.sendMessage(playerSender, "command.admin.help");
        } else {
            sender.sendMessage(LanguageHook.getComponent("command.admin.help", null));
        }
    }

    private void sendUsage(CommandSender sender, @Nullable Player playerSender, String usage) {
        sendMsg(sender, playerSender, "command.admin.usage", Placeholder.unparsed("usage", usage));
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
    public @NotNull Collection<String> suggest(@NotNull CommandSourceStack stack, @NotNull String[] args) {
        CommandSender sender = stack.getSender();
        if (sender == null || !sender.hasPermission("clarityskills.admin")) return List.of();

        if (args.length == 0) {
            return List.of("exp", "point", "boost", "level", "reset", "reload", "help");
        }

        if (args.length == 1) {
            List<String> subs = List.of("exp", "point", "boost", "level", "reset", "reload", "help");
            return subs.stream().filter(s -> s.startsWith(args[0].toLowerCase(Locale.ROOT))).toList();
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (sub.equals("exp")) {
            return switch (args.length) {
                case 2 -> List.of("give", "take", "set", "reset").stream().filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
                case 3 -> getPlayerSuggestions(args[2]);
                case 4 -> List.of("10", "50", "100", "500", "1000").stream().filter(s -> s.startsWith(args[3].toLowerCase(Locale.ROOT))).toList();
                case 5 -> List.of("true", "false").stream().filter(s -> s.startsWith(args[4].toLowerCase(Locale.ROOT))).toList();
                default -> List.of();
            };
        }

        if (sub.equals("point") || sub.equals("points")) {
            return switch (args.length) {
                case 2 -> List.of("give", "take", "set", "reset").stream().filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
                case 3 -> getPlayerSuggestions(args[2]);
                case 4 -> List.of("1", "5", "10", "50").stream().filter(s -> s.startsWith(args[3].toLowerCase(Locale.ROOT))).toList();
                default -> List.of();
            };
        }

        if (sub.equals("boost") || sub.equals("buff")) {
            if (args.length == 2) {
                return List.of("give", "take", "global", "clear", "list").stream().filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            } else if (args.length == 3) {
                if (args[1].equalsIgnoreCase("global")) {
                    return List.of("1.5", "2.0", "3.0").stream().filter(s -> s.startsWith(args[2].toLowerCase(Locale.ROOT))).toList();
                } else if (args[1].equalsIgnoreCase("clear")) {
                    List<String> clearOptions = new ArrayList<>(List.of("global"));
                    clearOptions.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).toList());
                    return clearOptions.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(args[2].toLowerCase(Locale.ROOT))).toList();
                }
                return getPlayerSuggestions(args[2]);
            } else if (args.length == 4) {
                if (args[1].equalsIgnoreCase("give")) {
                    return List.of("1.5", "2.0", "3.0").stream().filter(s -> s.startsWith(args[3].toLowerCase(Locale.ROOT))).toList();
                } else if (args[1].equalsIgnoreCase("global")) {
                    return List.of("30m", "1h", "2h", "1d", "perm").stream().filter(s -> s.startsWith(args[3].toLowerCase(Locale.ROOT))).toList();
                }
            } else if (args.length == 5 && args[1].equalsIgnoreCase("give")) {
                return List.of("15m", "30m", "1h", "1d", "perm").stream().filter(s -> s.startsWith(args[4].toLowerCase(Locale.ROOT))).toList();
            }
        }

        if (sub.equals("level")) {
            if (args.length == 2) return List.of("set").stream().filter(s -> s.startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            if (args.length == 3) return getPlayerSuggestions(args[2]);
            if (args.length == 4) return List.of("1", "10", "25", "50", "100").stream().filter(s -> s.startsWith(args[3].toLowerCase(Locale.ROOT))).toList();
        }

        if (sub.equals("reset")) {
            if (args.length == 2) return getPlayerSuggestions(args[1]);
            if (args.length == 3) return List.of("all", "stats", "points").stream().filter(s -> s.startsWith(args[2].toLowerCase(Locale.ROOT))).toList();
        }

        return List.of();
    }

    private Collection<String> getPlayerSuggestions(String current) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(current.toLowerCase()))
                .toList();
    }

    @Override
    public @Nullable String permission() {
        return "clarityskills.admin";
    }
}
