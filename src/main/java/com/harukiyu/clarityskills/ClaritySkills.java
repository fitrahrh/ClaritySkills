package com.harukiyu.clarityskills;

import com.harukiyu.clarityskills.api.ClaritySkillsAPI;
import com.harukiyu.clarityskills.command.SkillAdminCommand;
import com.harukiyu.clarityskills.command.SkillCommand;
import com.harukiyu.clarityskills.config.ConfigManager;
import com.harukiyu.clarityskills.data.StorageManager;
import com.harukiyu.clarityskills.dialog.SkillDialogManager;
import com.harukiyu.clarityskills.hook.LanguageHook;
import com.harukiyu.clarityskills.hook.PlaceholderHook;
import com.harukiyu.clarityskills.listener.PlayerConnectionListener;
import com.harukiyu.clarityskills.listener.StatCombatListener;
import com.harukiyu.clarityskills.manager.ExpBoostManager;
import com.harukiyu.clarityskills.manager.SkillManager;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class ClaritySkills extends JavaPlugin {
    private static ClaritySkills instance;

    private ConfigManager configManager;
    private StorageManager storageManager;
    private ExpBoostManager expBoostManager;
    private SkillManager skillManager;
    private SkillDialogManager dialogManager;
    private ScheduledTask autoSaveTask;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        // 1. Config & Language Hook
        this.configManager = new ConfigManager(this);
        this.configManager.reload();
        LanguageHook.init(this, "clarityskills");

        // 2. Data & Business Logic Managers
        this.storageManager = new StorageManager(this);
        this.expBoostManager = new ExpBoostManager(this);
        this.skillManager = new SkillManager(this);
        this.dialogManager = new SkillDialogManager(this);

        // 3. API Initialization
        ClaritySkillsAPI.init(this);

        // 4. Register Listeners
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        getServer().getPluginManager().registerEvents(new StatCombatListener(this), this);

        // 5. Register Commands (Paper 26.3 Native BasicCommand)
        registerCommand("skill", "Open skill status and allocation menu", List.of("skills"), new SkillCommand(this));
        registerCommand("skilladmin", "Admin commands for skills, exp, points and boost", List.of("skillsadmin", "skadmin", "sa"), new SkillAdminCommand(this));

        // 6. PlaceholderAPI Expansion
        if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new PlaceholderHook(this).register();
            getLogger().info("Successfully hooked into PlaceholderAPI!");
        }

        // 7. Auto-Save Task (Every 5 minutes)
        this.autoSaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(
                this,
                task -> storageManager.saveAll(false),
                5,
                5,
                TimeUnit.MINUTES
        );

        getLogger().log(Level.INFO, "{0} v{1} has been enabled!", new Object[]{getName(), getPluginMeta().getVersion()});
    }

    @Override
    public void onDisable() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }

        if (storageManager != null) {
            storageManager.saveAll(false);
        }

        getLogger().log(Level.INFO, "{0} has been disabled!", getName());
    }

    public static ClaritySkills getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public StorageManager getStorageManager() {
        return storageManager;
    }

    public ExpBoostManager getExpBoostManager() {
        return expBoostManager;
    }

    public SkillManager getSkillManager() {
        return skillManager;
    }

    public SkillDialogManager getDialogManager() {
        return dialogManager;
    }
}
