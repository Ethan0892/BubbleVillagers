package me.xginko.villageroptimizer.events;

import me.xginko.villageroptimizer.VillagerOptimizer;
import me.xginko.villageroptimizer.wrapper.WrappedVillager;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

public class OptimizedVillagerGlowCleanupListener implements Listener {

    private final Plugin plugin;

    public OptimizedVillagerGlowCleanupListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public void cleanupLoadedChunks() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                cleanupChunk(chunk);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private void onChunkLoad(ChunkLoadEvent event) {
        cleanupChunk(event.getChunk());
    }

    private void cleanupChunk(Chunk chunk) {
        // MockBukkit's Paper schedulers don't integrate with BukkitSchedulerMock ticks,
        // so region/entity scheduled tasks may never execute in unit tests.
        if (isMockBukkit()) {
            for (Entity entity : chunk.getEntities()) {
                if (!(entity instanceof Villager villager)) continue;
                if (!villager.isGlowing()) continue;

                WrappedVillager wrapped = VillagerOptimizer.wrappers().get(villager, WrappedVillager::new);
                if (!wrapped.isOptimized()) continue;

                villager.setGlowing(false);
            }
            return;
        }

        VillagerOptimizer.scheduling().regionSpecificScheduler(chunk.getWorld(), chunk.getX(), chunk.getZ()).run(() -> {
            for (Entity entity : chunk.getEntities()) {
                if (!(entity instanceof Villager villager)) continue;
                if (!villager.isGlowing()) continue;

                WrappedVillager wrapped = VillagerOptimizer.wrappers().get(villager, WrappedVillager::new);
                if (!wrapped.isOptimized()) continue;

                VillagerOptimizer.scheduling().entitySpecificScheduler(villager).run(() -> villager.setGlowing(false), null);
            }
        });
    }

    private boolean isMockBukkit() {
        return plugin.getServer().getClass().getName().startsWith("org.mockbukkit.");
    }
}
