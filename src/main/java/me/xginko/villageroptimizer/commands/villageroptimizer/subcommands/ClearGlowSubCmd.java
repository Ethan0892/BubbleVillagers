package me.xginko.villageroptimizer.commands.villageroptimizer.subcommands;

import me.xginko.villageroptimizer.VillagerOptimizer;
import me.xginko.villageroptimizer.commands.SubCommand;
import me.xginko.villageroptimizer.events.OptimizedVillagerGlowCleanupListener;
import me.xginko.villageroptimizer.struct.enums.Permissions;
import me.xginko.villageroptimizer.utils.KyoriUtil;
import me.xginko.villageroptimizer.utils.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class ClearGlowSubCmd extends SubCommand {

    public ClearGlowSubCmd() {
        super(
                "clearglow",
                Component.text("/villageroptimizer clearglow").color(Util.PL_COLOR),
                Component.text("Remove glow from optimized villagers in loaded chunks.").color(NamedTextColor.GRAY)
        );
    }

    @Override
    public @Nullable List<String> onTabComplete(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args
    ) {
        return Collections.emptyList();
    }

    @Override
    public boolean onCommand(
            @NotNull CommandSender sender, @NotNull Command command, @NotNull String commandLabel, @NotNull String[] args
    ) {
        if (!sender.hasPermission(Permissions.Commands.CLEAR_GLOW.get())) {
            KyoriUtil.sendMessage(sender, VillagerOptimizer.getLang(sender).no_permission);
            return true;
        }

        KyoriUtil.sendMessage(sender, Component.text("Starting glow cleanup for loaded chunks...").color(NamedTextColor.WHITE));
        new OptimizedVillagerGlowCleanupListener(VillagerOptimizer.getInstance()).cleanupLoadedChunks();
        KyoriUtil.sendMessage(sender, Component.text("Glow cleanup scheduled.").color(NamedTextColor.GREEN));
        KyoriUtil.sendMessage(sender, Component.text("Tip: only loaded chunks are affected; move around to load more.").color(NamedTextColor.GRAY));
        return true;
    }
}
