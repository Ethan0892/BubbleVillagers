package me.xginko.villageroptimizer;

import org.mockbukkit.mockbukkit.entity.PlayerMock;
import me.xginko.villageroptimizer.struct.enums.Keyring;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Villager;
import org.bukkit.persistence.PersistentDataType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClearGlowCommandTest extends MockBukkitTestBase {

    @Test
    void clearglowRemovesGlowFromOptimizedVillagersInLoadedChunks() {
        World world = server.addSimpleWorld("world");
        Villager villager = world.spawn(world.getSpawnLocation(), Villager.class);

        // MockBukkit may not consider any chunks "loaded" unless explicitly loaded.
        Chunk chunk = villager.getLocation().getChunk();
        chunk.load();

        villager.setGlowing(true);
        villager.getPersistentDataContainer().set(
                Keyring.VillagerOptimizer.OPTIMIZATION_TYPE.getKey(),
                PersistentDataType.STRING,
                "COMMAND"
        );

        PlayerMock admin = server.addPlayer();
        admin.setOp(true);

        boolean dispatched = server.dispatchCommand(admin, "villageroptimizer clearglow");
        assertTrue(dispatched);

        // allow scheduled tasks to run
        server.getScheduler().performTicks(5);

        assertFalse(villager.isGlowing());
    }
}
