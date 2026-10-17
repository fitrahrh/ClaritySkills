package com.harukiyu.clarityskills.data;

import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.model.StatType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class StorageManager {
    private final ClaritySkills plugin;
    private final File dataFolder;
    private final Map<UUID, PlayerSkillData> cache = new ConcurrentHashMap<>();

    public StorageManager(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "userdata");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
    }

    @NotNull
    public PlayerSkillData getPlayerData(@NotNull UUID uuid) {
        return cache.computeIfAbsent(uuid, this::loadFromFile);
    }

    @NotNull
    public PlayerSkillData getPlayerData(@NotNull Player player) {
        return getPlayerData(player.getUniqueId());
    }

    @NotNull
    private PlayerSkillData loadFromFile(@NotNull UUID uuid) {
        PlayerSkillData data = new PlayerSkillData(uuid);
        File file = new File(dataFolder, uuid + ".yml");
        if (!file.exists()) {
            return data;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        data.setLevel(config.getInt("level", 1));
        data.setCurrentExp(config.getLong("currentExp", 0));
        data.setTotalExp(config.getLong("totalExp", 0));
        data.setSkillPoints(config.getInt("skillPoints", 0));

        if (config.isConfigurationSection("stats")) {
            for (String key : config.getConfigurationSection("stats").getKeys(false)) {
                StatType stat = StatType.fromId(key);
                if (stat != null) {
                    data.setStat(stat, config.getInt("stats." + key, 0));
                }
            }
        }

        double boostMultiplier = config.getDouble("boost.multiplier", 1.0);
        long boostExpiry = config.getLong("boost.expiry", 0);
        data.setPersonalBoost(new ExpBoost(boostMultiplier, boostExpiry, true));
        data.setDirty(false);

        return data;
    }

    public void savePlayerData(@NotNull UUID uuid, boolean async) {
        PlayerSkillData data = cache.get(uuid);
        if (data == null || !data.isDirty()) return;

        Runnable task = () -> {
            File file = new File(dataFolder, uuid + ".yml");
            YamlConfiguration config = new YamlConfiguration();

            config.set("level", data.getLevel());
            config.set("currentExp", data.getCurrentExp());
            config.set("totalExp", data.getTotalExp());
            config.set("skillPoints", data.getSkillPoints());

            for (StatType type : StatType.values()) {
                config.set("stats." + type.getId(), data.getStat(type));
            }

            ExpBoost boost = data.getPersonalBoost();
            config.set("boost.multiplier", boost.getMultiplier());
            config.set("boost.expiry", boost.getExpiryTimestamp());

            try {
                config.save(file);
                data.setDirty(false);
            } catch (IOException e) {
                plugin.getLogger().warning("Failed to save player data for " + uuid + ": " + e.getMessage());
            }
        };

        if (async && plugin.isEnabled()) {
            Bukkit.getAsyncScheduler().runNow(plugin, scheduledTask -> task.run());
        } else {
            task.run();
        }
    }

    public void saveAll(boolean async) {
        for (UUID uuid : cache.keySet()) {
            savePlayerData(uuid, async);
        }
    }

    public void unloadPlayer(@NotNull UUID uuid) {
        savePlayerData(uuid, false);
        cache.remove(uuid);
    }
}
