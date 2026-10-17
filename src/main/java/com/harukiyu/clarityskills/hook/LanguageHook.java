package com.harukiyu.clarityskills.hook;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class LanguageHook {

    private static boolean isClarityLangCorePresent = false;
    private static final Map<String, YamlConfiguration> fallbackBundles = new ConcurrentHashMap<>();
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static final List<String> SUPPORTED_LANGS = List.of(
            "en_US", "id_ID", "pt_BR", "es_ES", "zh_CN", "ja_JP", "de_DE", "fr_FR", "ru_RU"
    );

    private LanguageHook() {}

    public static void init(@NotNull Plugin plugin, @NotNull String namespace) {
        fallbackBundles.clear();
        for (String lang : SUPPORTED_LANGS) {
            try (InputStream stream = plugin.getResource("languages/" + lang + ".yml")) {
                if (stream != null) {
                    YamlConfiguration config = YamlConfiguration.loadConfiguration(new InputStreamReader(stream, StandardCharsets.UTF_8));
                    fallbackBundles.put(lang.toLowerCase(Locale.ROOT), config);
                }
            } catch (Throwable ignored) {}
        }

        if (Bukkit.getPluginManager().isPluginEnabled("ClarityLangCore")) {
            try {
                ClarityLangAPI.registerBundle(plugin, namespace, "languages");
                isClarityLangCorePresent = true;
                plugin.getLogger().info("Successfully hooked into ClarityLangCore bundle [" + namespace + "]!");
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
                if (code != null && !code.isBlank()) return code;
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

        if (isClarityLangCorePresent && player != null) {
            try {
                return ClarityLangAPI.getComponent(key, player, enriched);
            } catch (Throwable ignored) {}
        }
        return getFallbackComponent(key, langCode, enriched);
    }

    @Nullable
    public static String getRawString(@NotNull String key, @NotNull String langCode) {
        if (isClarityLangCorePresent) {
            try {
                String val = ClarityLangAPI.getRawString(key, langCode);
                if (val != null) return val;
            } catch (Throwable ignored) {}
        }
        YamlConfiguration config = fallbackBundles.get(langCode.toLowerCase(Locale.ROOT));
        if (config == null) config = fallbackBundles.get("en_us");
        return config != null ? config.getString(key) : null;
    }

    @NotNull
    private static Component getFallbackComponent(@NotNull String key, @NotNull String langCode, @NotNull TagResolver... resolvers) {
        String raw = getRawString(key, langCode);
        if (raw == null) raw = key;
        return MINI_MESSAGE.deserialize(raw, resolvers);
    }

    @NotNull
    private static TagResolver[] buildResolversWithPrefix(String langCode, @NotNull TagResolver... resolvers) {
        String prefix = getRawString("prefix", langCode);
        if (prefix == null) prefix = "";
        List<TagResolver> list = new ArrayList<>(resolvers.length + 1);
        list.add(Placeholder.parsed("prefix", prefix));
        Collections.addAll(list, resolvers);
        return list.toArray(TagResolver[]::new);
    }

    public static boolean isClarityLangCorePresent() {
        return isClarityLangCorePresent;
    }
}
