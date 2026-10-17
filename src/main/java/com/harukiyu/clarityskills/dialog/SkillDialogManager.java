package com.harukiyu.clarityskills.dialog;

import com.harukiyu.claritylangcore.api.ClarityLangAPI;
import com.harukiyu.clarityskills.ClaritySkills;
import com.harukiyu.clarityskills.api.model.ElementalTier;
import com.harukiyu.clarityskills.api.model.StatType;
import com.harukiyu.clarityskills.config.ConfigManager;
import com.harukiyu.clarityskills.data.PlayerSkillData;
import com.harukiyu.clarityskills.hook.LanguageHook;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.TextDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class SkillDialogManager {

    public static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(-1)
            .lifetime(Duration.ofDays(365))
            .build();

    private final ClaritySkills plugin;

    public SkillDialogManager(@NotNull ClaritySkills plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(@NotNull Player player) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        ConfigManager config = plugin.getConfigManager();

        long reqExp = config.calculateRequiredExp(data.getLevel());
        double expPercent = reqExp > 0 ? ((double) data.getCurrentExp() / reqExp) * 100.0 : 100.0;
        double effectiveMultiplier = plugin.getExpBoostManager().getEffectiveMultiplier(player);

        Component title = LanguageHook.getComponent("dialog.main.title", player);
        Component body = LanguageHook.getComponent("dialog.main.body", player,
                Placeholder.unparsed("level", String.valueOf(data.getLevel())),
                Placeholder.unparsed("max_level", String.valueOf(config.getMaxLevel())),
                Placeholder.unparsed("current_exp", String.valueOf(data.getCurrentExp())),
                Placeholder.unparsed("req_exp", String.valueOf(reqExp)),
                Placeholder.unparsed("exp_percent", String.format("%.1f", expPercent)),
                Placeholder.unparsed("points", String.valueOf(data.getSkillPoints())),
                Placeholder.unparsed("boost", String.format("%.1fx", effectiveMultiplier)),
                Placeholder.unparsed("atk", String.valueOf(data.getStat(StatType.ATTACK))),
                Placeholder.unparsed("matk", String.valueOf(data.getStat(StatType.MAGIC))),
                Placeholder.unparsed("def", String.valueOf(data.getStat(StatType.DEFENSE))),
                Placeholder.unparsed("fire", String.valueOf(data.getStat(StatType.FIRE))),
                Placeholder.unparsed("wind", String.valueOf(data.getStat(StatType.WIND))),
                Placeholder.unparsed("water", String.valueOf(data.getStat(StatType.WATER))),
                Placeholder.unparsed("earth", String.valueOf(data.getStat(StatType.EARTH))),
                Placeholder.unparsed("light", String.valueOf(data.getStat(StatType.LIGHT)))
        );

        List<ActionButton> buttons = new ArrayList<>();

        // 1. Allocate Stats Button
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_allocate", player))
                .tooltip(LanguageHook.getComponent("dialog.main.btn_allocate_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openAttributeListDialog(p);
                }, CALLBACK_OPTIONS))
                .build());

        // 2. Elemental Masteries Button
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_elements", player))
                .tooltip(LanguageHook.getComponent("dialog.main.btn_elements_tooltip", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openElementalTiersDialog(p);
                }, CALLBACK_OPTIONS))
                .build());

        // 3. Language settings (if ClarityLangCore installed)
        if (LanguageHook.isClarityLangCorePresent()) {
            buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.main.btn_language", player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) {
                            p.closeDialog();
                            try {
                                ClarityLangAPI.openLanguageDialog(p);
                            } catch (Throwable ignored) {}
                        }
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        // Close Button
        ActionButton exitBtn = ActionButton.builder(LanguageHook.getComponent("dialog.general.btn_close", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) p.closeDialog();
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, exitBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openAttributeListDialog(@NotNull Player player) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        ConfigManager config = plugin.getConfigManager();

        Component title = LanguageHook.getComponent("dialog.attributes.title", player);
        Component body = LanguageHook.getComponent("dialog.attributes.body", player,
                Placeholder.unparsed("points", String.valueOf(data.getSkillPoints())));

        List<ActionButton> buttons = new ArrayList<>();

        for (StatType stat : StatType.values()) {
            int currentLevel = data.getStat(stat);
            Component statName = LanguageHook.getComponent(stat.getNameKey(), player);

            Component label = LanguageHook.getComponent("dialog.attributes.stat_entry", player,
                    Placeholder.component("stat", statName),
                    Placeholder.unparsed("level", String.valueOf(currentLevel)),
                    Placeholder.unparsed("max", String.valueOf(config.getStatMaxLevel())));

            buttons.add(ActionButton.builder(label)
                    .tooltip(LanguageHook.getComponent(stat.getDescKey(), player))
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openStatUpgradeDialog(p, stat);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainMenu(p);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openStatUpgradeDialog(@NotNull Player player, @NotNull StatType stat) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        ConfigManager config = plugin.getConfigManager();

        int currentLevel = data.getStat(stat);
        int points = data.getSkillPoints();

        Component title = LanguageHook.getComponent("dialog.upgrade.title", player,
                Placeholder.component("stat", LanguageHook.getComponent(stat.getNameKey(), player)));

        Component body = LanguageHook.getComponent("dialog.upgrade.body", player,
                Placeholder.component("stat", LanguageHook.getComponent(stat.getNameKey(), player)),
                Placeholder.component("desc", LanguageHook.getComponent(stat.getDescKey(), player)),
                Placeholder.unparsed("level", String.valueOf(currentLevel)),
                Placeholder.unparsed("max", String.valueOf(config.getStatMaxLevel())),
                Placeholder.unparsed("points", String.valueOf(points)),
                Placeholder.unparsed("cost", String.valueOf(config.getCostPerStatPoint()))
        );

        List<ActionButton> buttons = new ArrayList<>();

        // Quick allocate +1
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.upgrade.btn_plus_1", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        plugin.getSkillManager().allocateStat(p, stat, 1);
                        openStatUpgradeDialog(p, stat);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Quick allocate +5
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.upgrade.btn_plus_5", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        plugin.getSkillManager().allocateStat(p, stat, 5);
                        openStatUpgradeDialog(p, stat);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Quick allocate +10
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.upgrade.btn_plus_10", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        plugin.getSkillManager().allocateStat(p, stat, 10);
                        openStatUpgradeDialog(p, stat);
                    }
                }, CALLBACK_OPTIONS))
                .build());

        // Custom amount input dialog
        buttons.add(ActionButton.builder(LanguageHook.getComponent("dialog.upgrade.btn_custom", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openCustomAmountDialog(p, stat);
                }, CALLBACK_OPTIONS))
                .build());

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openAttributeListDialog(p);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 2))
        );

        player.showDialog(dialog);
    }

    public void openCustomAmountDialog(@NotNull Player player, @NotNull StatType stat) {
        Component title = LanguageHook.getComponent("dialog.custom.title", player);
        Component body = LanguageHook.getComponent("dialog.custom.body", player,
                Placeholder.component("stat", LanguageHook.getComponent(stat.getNameKey(), player)));
        Component label = LanguageHook.getComponent("dialog.custom.label", player);

        TextDialogInput amountInput = DialogInput.text("points_amount", label)
                .initial("1")
                .maxLength(4)
                .build();

        ActionButton confirmBtn = ActionButton.builder(LanguageHook.getComponent("dialog.general.btn_confirm", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) {
                        String input = view.getText("points_amount");
                        int amount;
                        try {
                            amount = Integer.parseInt(input);
                            if (amount <= 0) throw new NumberFormatException();
                        } catch (NumberFormatException e) {
                            LanguageHook.sendMessage(p, "dialog.custom.invalid_amount");
                            return;
                        }

                        plugin.getSkillManager().allocateStat(p, stat, amount);
                        openStatUpgradeDialog(p, stat);
                    }
                }, CALLBACK_OPTIONS))
                .build();

        ActionButton cancelBtn = ActionButton.builder(LanguageHook.getComponent("dialog.general.btn_cancel", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openStatUpgradeDialog(p, stat);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .inputs(List.of(amountInput))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.confirmation(confirmBtn, cancelBtn))
        );

        player.showDialog(dialog);
    }

    public void openElementalTiersDialog(@NotNull Player player) {
        PlayerSkillData data = plugin.getStorageManager().getPlayerData(player);
        ConfigManager config = plugin.getConfigManager();

        Component title = LanguageHook.getComponent("dialog.elements.title", player);
        Component body = LanguageHook.getComponent("dialog.elements.body", player);

        List<ActionButton> buttons = new ArrayList<>();

        for (StatType element : StatType.getElementalStats()) {
            int level = data.getStat(element);
            ElementalTier tier = data.getElementalTier(element, config.getTier1Threshold(), config.getTier2Threshold());

            ElementalTier t1 = ElementalTier.getTier1(element);
            ElementalTier t2 = ElementalTier.getTier2(element);

            String t1Name = t1 != null ? LanguageHook.getRawString(t1.getNameKey(), LanguageHook.getEffectiveLanguageCode(player)) : "T1";
            String t2Name = t2 != null ? LanguageHook.getRawString(t2.getNameKey(), LanguageHook.getEffectiveLanguageCode(player)) : "T2";

            Component btnLabel = LanguageHook.getComponent("dialog.elements.entry", player,
                    Placeholder.component("element", LanguageHook.getComponent(element.getNameKey(), player)),
                    Placeholder.component("tier", LanguageHook.getComponent(tier.getNameKey(), player)),
                    Placeholder.unparsed("level", String.valueOf(level)));

            Component tooltip = LanguageHook.getComponent("dialog.elements.entry_tooltip", player,
                    Placeholder.unparsed("t1_name", t1Name != null ? t1Name : ""),
                    Placeholder.unparsed("t1_req", String.valueOf(config.getTier1Threshold())),
                    Placeholder.unparsed("t2_name", t2Name != null ? t2Name : ""),
                    Placeholder.unparsed("t2_req", String.valueOf(config.getTier2Threshold())));

            buttons.add(ActionButton.builder(btnLabel)
                    .tooltip(tooltip)
                    .action(DialogAction.customClick((view, audience) -> {
                        if (audience instanceof Player p) openStatUpgradeDialog(p, element);
                    }, CALLBACK_OPTIONS))
                    .build());
        }

        ActionButton backBtn = ActionButton.builder(LanguageHook.getComponent("dialog.general.btn_back", player))
                .action(DialogAction.customClick((view, audience) -> {
                    if (audience instanceof Player p) openMainMenu(p);
                }, CALLBACK_OPTIONS))
                .build();

        Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .body(List.of(DialogBody.plainMessage(body)))
                        .canCloseWithEscape(true)
                        .build())
                .type(DialogType.multiAction(buttons, backBtn, 1))
        );

        player.showDialog(dialog);
    }
}
