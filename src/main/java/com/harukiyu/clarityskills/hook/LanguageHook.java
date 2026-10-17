package com.harukiyu.clarityskills.hook;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public final class LanguageHook {

    private static String namespace = "clarityskills";
    private static boolean isClarityLangCorePresent = false;
    private static final Map<String, YamlConfiguration> fallbackBundles = new ConcurrentHashMap<>();
    private static final Map<String, YamlConfiguration> jarBundles = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static final List<String> SUPPORTED_LANGS = List.of(
            "en_US", "id_ID", "pt_BR", "es_ES", "zh_CN", "ja_JP", "de_DE", "fr_FR", "ru_RU"
    );

    private static final Map<String, String> KEY_ALIASES = Map.of(
            "command.admin.points_given", "command.admin.point_given",
            "command.admin.point_given", "command.admin.points_given",
            "command.admin.points_taken", "command.admin.point_taken",
            "command.admin.point_taken", "command.admin.points_taken",
            "command.admin.points_set", "command.admin.point_set",
            "command.admin.point_set", "command.admin.points_set",
            "command.admin.points_reset", "command.admin.point_reset",
            "command.admin.point_reset", "command.admin.points_reset",
            "command.admin.boost_cleared", "command.admin.boost_cleared_player",
            "command.admin.boost_cleared_player", "command.admin.boost_cleared"
    );

    private LanguageHook() {}

    public static void init(@NotNull Plugin plugin, @NotNull String ns) {
        namespace = ns.toLowerCase(Locale.ROOT);
        fallbackBundles.clear();
        jarBundles.clear();

        File langFolder = new File(plugin.getDataFolder(), "languages");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        for (String lang : SUPPORTED_LANGS) {
            String lowerLang = lang.toLowerCase(Locale.ROOT);
            File langFile = new File(langFolder, lang + ".yml");
            if (!langFile.exists()) {
                try {
                    plugin.saveResource("languages/" + lang + ".yml", false);
                } catch (Throwable ignored) {}
            }

            YamlConfiguration jarConfig = null;
            try (InputStream stream = plugin.getResource("languages/" + lang + ".yml")) {
                if (stream != null) {
                    jarConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    jarBundles.put(lowerLang, jarConfig);
                }
            } catch (IOException ignored) {}

            YamlConfiguration config = null;
            if (langFile.exists()) {
                config = YamlConfiguration.loadConfiguration(langFile);
                if (jarConfig != null) {
                    boolean modified = false;
                    for (String key : jarConfig.getKeys(true)) {
                        if (!config.contains(key)) {
                            config.set(key, jarConfig.get(key));
                            modified = true;
                        }
                    }
                    if (config.contains("dialog.elements.body")) {
                        String bodyStr = config.getString("dialog.elements.body");
                        if (bodyStr != null && (bodyStr.contains("•") || bodyStr.contains("<gray>•"))) {
                            config.set("dialog.elements.body", jarConfig.get("dialog.elements.body"));
                            modified = true;
                        }
                    }
                    if (modified) {
                        try {
                            config.save(langFile);
                        } catch (IOException ignored) {}
                    }
                    config.setDefaults(jarConfig);
                }
            } else if (jarConfig != null) {
                config = jarConfig;
            }

            if (config != null) {
                fallbackBundles.put(lowerLang, config);
            }
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ClarityLangCore")) {
            try {
                ClarityLangAPI.registerBundle(plugin, namespace, "languages");
                if (langFolder.exists()) {
                    try {
                        ClarityLangAPI.registerBundle(namespace, langFolder);
                    } catch (Throwable ignored) {}
                }
                isClarityLangCorePresent = true;
                plugin.getLogger().log(Level.INFO, "Successfully hooked into ClarityLangCore bundle [{0}]!", namespace);
            } catch (Throwable t) {
                isClarityLangCorePresent = false;
            }
        } else {
            isClarityLangCorePresent = false;
        }
    }

    @NotNull
    public static String getEffectiveLanguageCode(@Nullable Player player) {
        if (player == null) return "en_US";
        if (isClarityLangCorePresent) {
            try {
                String code = ClarityLangAPI.getPlayerLanguageCode(player);
                if (!code.isBlank()) return code;
            } catch (Throwable ignored) {}
        }
        try {
            Locale clientLocale = player.locale();
            String lang = clientLocale.getLanguage().toLowerCase(Locale.ROOT);
            for (String supported : SUPPORTED_LANGS) {
                if (supported.toLowerCase(Locale.ROOT).startsWith(lang)) return supported;
            }
        } catch (Throwable ignored) {}
        return "en_US";
    }

    public static void sendMessage(@Nullable Player player, @NotNull String key, @NotNull TagResolver... resolvers) {
        if (player == null) return;
        player.sendMessage(getComponent(key, player, resolvers));
    }

    @NotNull
    public static Component getComponent(@NotNull String key, @Nullable Player player, @NotNull TagResolver... resolvers) {
        String langCode = getEffectiveLanguageCode(player);
        TagResolver[] enriched = buildResolversWithPrefix(langCode, resolvers);
        String cleanKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        String fullKey = key.contains(":") ? key : namespace + ":" + key;

        if (isClarityLangCorePresent) {
            Component comp = tryClarityLangCore(fullKey, cleanKey, player, langCode, enriched);
            if (comp != null) return comp;

            if (KEY_ALIASES.containsKey(cleanKey)) {
                String aliasClean = KEY_ALIASES.get(cleanKey);
                String aliasFull = namespace + ":" + aliasClean;
                Component aliasComp = tryClarityLangCore(aliasFull, aliasClean, player, langCode, enriched);
                if (aliasComp != null) return aliasComp;
            }
        }
        return getFallbackComponent(cleanKey, langCode, enriched);
    }

    @Nullable
    private static Component tryClarityLangCore(String fullKey, String cleanKey, @Nullable Player player, String langCode, TagResolver[] resolvers) {
        try {
            Component comp = player != null
                    ? ClarityLangAPI.getComponent(fullKey, player, resolvers)
                    : ClarityLangAPI.getComponent(fullKey, langCode, resolvers);

            if (comp instanceof TextComponent tc) {
                String content = tc.content();
                if (content.equals("<" + fullKey + ">") || content.equals("<" + cleanKey + ">")
                        || content.equals(fullKey) || content.equals(cleanKey)) {
                    return null;
                }
            }
            return comp;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    public static String getRawString(@NotNull String key, @NotNull String langCode) {
        String cleanKey = key.contains(":") ? key.substring(key.indexOf(':') + 1) : key;
        String fullKey = key.contains(":") ? key : namespace + ":" + key;

        if (isClarityLangCorePresent) {
            String val = tryClarityLangCoreRaw(fullKey, cleanKey, langCode);
            if (val != null) return val;

            if (KEY_ALIASES.containsKey(cleanKey)) {
                String aliasClean = KEY_ALIASES.get(cleanKey);
                String aliasFull = namespace + ":" + aliasClean;
                val = tryClarityLangCoreRaw(aliasFull, aliasClean, langCode);
                if (val != null) return val;
            }
        }
        return resolveRawKey(cleanKey, langCode);
    }

    @Nullable
    private static String tryClarityLangCoreRaw(String fullKey, String cleanKey, String langCode) {
        try {
            String val = ClarityLangAPI.getRawString(fullKey, langCode);
            if (val != null && !val.equals("<" + fullKey + ">") && !val.equals("<" + cleanKey + ">")
                    && !val.equals(fullKey) && !val.equals(cleanKey)) {
                return val;
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @NotNull
    private static Component getFallbackComponent(@NotNull String cleanKey, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        String raw = resolveRawKey(cleanKey, langCode);
        if (raw == null) raw = cleanKey;
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }

    @Nullable
    private static String resolveRawKey(@NotNull String cleanKey, @NotNull String langCode) {
        String lowerLang = langCode.toLowerCase(Locale.ROOT);

        // 1. Try disk fallback bundle for requested lang
        String raw = getRawFromConfig(fallbackBundles.get(lowerLang), cleanKey);
        if (raw != null) return raw;

        // 2. Try in-memory jar bundle for requested lang
        raw = getRawFromConfig(jarBundles.get(lowerLang), cleanKey);
        if (raw != null) return raw;

        // 3. Try disk fallback bundle for en_us
        raw = getRawFromConfig(fallbackBundles.get("en_us"), cleanKey);
        if (raw != null) return raw;

        // 4. Try in-memory jar bundle for en_us
        return getRawFromConfig(jarBundles.get("en_us"), cleanKey);
    }

    @Nullable
    private static String getRawFromConfig(@Nullable YamlConfiguration config, @NotNull String cleanKey) {
        if (config == null) return null;
        String val = config.getString(cleanKey);
        if (val == null && KEY_ALIASES.containsKey(cleanKey)) {
            val = config.getString(KEY_ALIASES.get(cleanKey));
        }
        return val;
    }

    @NotNull
    private static TagResolver[] buildResolversWithPrefix(String langCode, @NotNull TagResolver... resolvers) {
        String prefix = resolveRawKey("prefix", langCode);
        if (prefix == null) prefix = "<gradient:#FFAA00:#FF5555>[ClaritySkills]</gradient> <gray>";
        List<TagResolver> list = new ArrayList<>(resolvers.length + 1);
        list.add(Placeholder.parsed("prefix", prefix));
        Collections.addAll(list, resolvers);
        return list.toArray(TagResolver[]::new);
    }

    public static boolean isClarityLangCorePresent() {
        return isClarityLangCorePresent;
    }
}
