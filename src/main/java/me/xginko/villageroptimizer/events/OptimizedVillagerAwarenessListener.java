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
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;

/**
 * Keeps the awareness flag of optimized villagers in sync with their stored optimization type.
 * <p>
 * Optimization is held in two independent places: the OptimizationType in the villager's
 * persistent data container, which is written once, and the entity's awareness flag, which the
 * plugin disables once and never checks again. Whenever awareness is turned back on temporarily,
 * as {@code LevelOptimizedProfession} does while a villager levels up, an unload or a server stop
 * in that window persists an aware villager that the plugin still considers optimized. Nothing
 * brings the two back into agreement, so the villager keeps ticking its AI forever.
 * <p>
 * Villagers are re-checked when their entities load, and once for everything already loaded when
 * the listener starts. {@link EntitiesLoadEvent} is used rather than {@code ChunkLoadEvent} because
 * entities are loaded separately from their chunk and may not be attached yet when the latter fires.
 */
public class OptimizedVillagerAwarenessListener implements Listener {

    private final Plugin plugin;

    public OptimizedVillagerAwarenessListener(Plugin plugin) {
        this.plugin = plugin;
    }

    public void syncLoadedChunks() {
        for (World world : plugin.getServer().getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                // MockBukkit's Paper schedulers don't integrate with BukkitSchedulerMock ticks,
                // so region/entity scheduled tasks may never execute in unit tests.
                if (isMockBukkit()) {
                    sync(Arrays.asList(chunk.getEntities()));
                } else {
                    VillagerOptimizer.scheduling().regionSpecificScheduler(chunk.getWorld(), chunk.getX(), chunk.getZ())
                            .run(() -> sync(Arrays.asList(chunk.getEntities())));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    private void onEntitiesLoad(EntitiesLoadEvent event) {
        sync(event.getEntities());
    }

    private void sync(Iterable<Entity> entities) {
        for (Entity entity : entities) {
            if (!(entity instanceof Villager villager)) continue;
            if (!needsAwarenessRemoved(villager)) continue;

            if (isMockBukkit()) {
                villager.setAware(false);
                continue;
            }

            VillagerOptimizer.scheduling().entitySpecificScheduler(villager).run(() -> {
                // Leave a villager alone while a player has its trading screen open,
                // it will be picked up again the next time its entities load.
                if (villager.isTrading()) return;
                if (!needsAwarenessRemoved(villager)) return;

                villager.setAware(false);
            }, null);
        }
    }

    private boolean needsAwarenessRemoved(Villager villager) {
        if (!villager.isAware()) return false;

        WrappedVillager wrapped = VillagerOptimizer.wrappers().get(villager, WrappedVillager::new);
        return wrapped.isOptimized();
    }

    private boolean isMockBukkit() {
        return plugin.getServer().getClass().getName().startsWith("org.mockbukkit.");
    }
}
