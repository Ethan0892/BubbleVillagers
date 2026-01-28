package me.xginko.villageroptimizer;

import me.xginko.villageroptimizer.struct.enums.Keyring;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutoOptimizeModesTest extends MockBukkitTestBase {

    @Test
    void autoOptimizeChunkPopulationOptimizesWhenThresholdExceeded() {
        World world = server.addSimpleWorld("world");
        Chunk chunk = world.getChunkAt(world.getSpawnLocation());

        // enable module
        VillagerOptimizer.config().master().set("optimization-methods.auto-optimize-chunk-population.enable", true);
        VillagerOptimizer.config().master().set("optimization-methods.auto-optimize-chunk-population.threshold", 1);
        VillagerOptimizer.config().master().set("optimization-methods.auto-optimize-chunk-population.check-period-ticks", 20);
        VillagerOptimizer.config().saveConfig();
        plugin.reloadPlugin();

        Villager v1 = world.spawn(chunk.getBlock(0, 80, 0).getLocation(), Villager.class);
        Villager v2 = world.spawn(chunk.getBlock(1, 80, 0).getLocation(), Villager.class);

        // let module scan + optimization write hit PDC
        server.getScheduler().performTicks(40);

        assertTrue(v1.getPersistentDataContainer().has(Keyring.VillagerOptimizer.OPTIMIZATION_TYPE.getKey(), PersistentDataType.STRING));
        assertTrue(v2.getPersistentDataContainer().has(Keyring.VillagerOptimizer.OPTIMIZATION_TYPE.getKey(), PersistentDataType.STRING));
    }

    @Test
    void autoOptimizeTradeHallPopulationOptimizesWithinRadius() {
        World world = server.addSimpleWorld("world");

        // enable module
        VillagerOptimizer.config().master().set("optimization-methods.auto-optimize-trade-hall-population.enable", true);
        VillagerOptimizer.config().master().set("optimization-methods.auto-optimize-trade-hall-population.population", 2);
        VillagerOptimizer.config().master().set("optimization-methods.auto-optimize-trade-hall-population.radius-blocks", 10.0);
        VillagerOptimizer.config().master().set("optimization-methods.auto-optimize-trade-hall-population.check-period-ticks", 20);
        VillagerOptimizer.config().saveConfig();
        plugin.reloadPlugin();

        Villager v1 = world.spawn(world.getSpawnLocation().clone().add(0, 0, 0), Villager.class);
        Villager v2 = world.spawn(world.getSpawnLocation().clone().add(2, 0, 0), Villager.class);

        server.getScheduler().performTicks(40);

        assertTrue(v1.getPersistentDataContainer().has(Keyring.VillagerOptimizer.OPTIMIZATION_TYPE.getKey(), PersistentDataType.STRING));
        assertTrue(v2.getPersistentDataContainer().has(Keyring.VillagerOptimizer.OPTIMIZATION_TYPE.getKey(), PersistentDataType.STRING));
    }
}
