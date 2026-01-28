package me.xginko.villageroptimizer.commands.villageroptimizer.subcommands;

import me.xginko.villageroptimizer.VillagerOptimizer;
import me.xginko.villageroptimizer.commands.SubCommand;
import me.xginko.villageroptimizer.struct.enums.Permissions;
import me.xginko.villageroptimizer.utils.KyoriUtil;
import me.xginko.villageroptimizer.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GlowSubCmd extends SubCommand {

    private static final String PATH = "gameplay.outline-optimized-villagers.enable";

    public GlowSubCmd() {
        super(
                "glow",
                Component.text("/villageroptimizer glow <on|off>").color(Util.PL_COLOR),
                Component.text("Enable/disable glowing outline for optimized villagers.").color(NamedTextColor.GRAY)
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args
    ) {
        if (args.length == 2) {
            return Arrays.asList("on", "off");
        }
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args
    ) {
        if (!sender.hasPermission(Permissions.Commands.GLOW.get())) {
            KyoriUtil.sendMessage(sender, VillagerOptimizer.getLang(sender).no_permission);
            return true;
        }

        if (args.length < 2) {
            boolean enabled = VillagerOptimizer.config().getBoolean(PATH, false);
            KyoriUtil.sendMessage(sender,
                    Component.text("Optimized villager glow is currently ")
                            .color(NamedTextColor.GRAY)
                            .append(Component.text(enabled ? "ON" : "OFF")
                                    .color(enabled ? NamedTextColor.GREEN : NamedTextColor.RED)));
            KyoriUtil.sendMessage(sender, Component.text("Usage: /villageroptimizer glow <on|off>").color(NamedTextColor.GRAY));
            return true;
        }

        Boolean newValue = null;
        if (args[1].equalsIgnoreCase("on") || args[1].equalsIgnoreCase("true") || args[1].equalsIgnoreCase("enable")) {
            newValue = true;
        } else if (args[1].equalsIgnoreCase("off") || args[1].equalsIgnoreCase("false") || args[1].equalsIgnoreCase("disable")) {
            newValue = false;
        }

        if (newValue == null) {
            KyoriUtil.sendMessage(sender, Component.text("Invalid value. Use on/off.").color(NamedTextColor.RED));
            return true;
        }

        // Update config and reload so modules/listeners apply immediately.
        VillagerOptimizer.config().master().set(PATH, newValue);
        VillagerOptimizer.config().saveConfig();

        final boolean finalNewValue = newValue;
        KyoriUtil.sendMessage(sender, Component.text("Applying glow setting...").color(NamedTextColor.WHITE));
        VillagerOptimizer.scheduling().asyncScheduler().run(task -> {
            VillagerOptimizer.getInstance().reloadPlugin();
            KyoriUtil.sendMessage(sender,
                    Component.text("Optimized villager glow is now ")
                            .color(NamedTextColor.GRAY)
                            .append(Component.text(finalNewValue ? "ON" : "OFF")
                                    .color(finalNewValue ? NamedTextColor.GREEN : NamedTextColor.RED)));
            if (!finalNewValue) {
                KyoriUtil.sendMessage(sender, Component.text("Glow will be cleared from loaded chunks.").color(NamedTextColor.GRAY));
            }
        });
        return true;
    }
}
