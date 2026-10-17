package com.harukiyu.clarityskills;

import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import io.papermc.paper.registry.event.RegistryEvents;
import io.papermc.paper.registry.keys.DialogKeys;
import io.papermc.paper.registry.keys.tags.DialogTagKeys;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;

public class ClaritySkillsBootstrap implements PluginBootstrap {
    private static final TypedKey<Dialog> PAUSE_DIALOG = DialogKeys.create(Key.key("clarityskills", "skills_menu"));

    public static final ClickCallback.Options CALLBACK_OPTIONS = ClickCallback.Options.builder()
            .uses(-1)
            .lifetime(Duration.ofDays(365))
            .build();

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        try {
            // 1. Register Dialog in Registry
            context.getLifecycleManager().registerEventHandler(RegistryEvents.DIALOG.compose(), event -> {
                try {
                    ActionButton openBtn = ActionButton.builder(Component.text("Open Skills Menu"))
                            .action(DialogAction.customClick((view, audience) -> {
                                if (audience instanceof Player p) {
                                    p.closeDialog();
                                    p.performCommand("skill");
                                }
                            }, CALLBACK_OPTIONS))
                            .build();

                    ActionButton closeBtn = ActionButton.builder(Component.text("Close"))
                            .action(DialogAction.customClick((view, audience) -> {
                                if (audience instanceof Player p) {
                                    p.closeDialog();
                                }
                            }, CALLBACK_OPTIONS))
                            .build();

                    event.registry().register(PAUSE_DIALOG, builder -> builder
                            .base(DialogBase.builder(Component.text("CLARITY SKILLS"))
                                    .externalTitle(Component.text("Clarity Skills"))
                                    .body(List.of(DialogBody.plainMessage(Component.text("View your RPG skills, stats and elemental tiers."))))
                                    .canCloseWithEscape(true)
                                    .build())
                            .type(DialogType.multiAction(List.of(openBtn), closeBtn, 1))
                    );
                } catch (Throwable t) {
                    context.getLogger().warn("Failed to compose skills DIALOG: " + t.getMessage());
                }
            });

            // 2. Attach to pause screen additions
            context.getLifecycleManager().registerEventHandler(
                    LifecycleEvents.TAGS.postFlatten(RegistryKey.DIALOG),
                    event -> {
                        try {
                            event.registrar().addToTag(DialogTagKeys.PAUSE_SCREEN_ADDITIONS, List.of(PAUSE_DIALOG));
                        } catch (Throwable t) {
                            context.getLogger().warn("Failed to add to pause tag: " + t.getMessage());
                        }
                    }
            );
        } catch (Throwable t) {
            context.getLogger().warn("Dialog lifecycle hook initialization notice: " + t.getMessage());
        }
    }
}
