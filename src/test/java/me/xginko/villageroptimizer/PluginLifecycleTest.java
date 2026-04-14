package me.xginko.villageroptimizer;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Disabled("MockBukkit does not yet support Paper 26.x registry changes - re-enable when mockbukkit-v26 is released")
class PluginLifecycleTest extends MockBukkitTestBase {

    @Test
    void pluginLoads() {
        assertNotNull(plugin);
        assertTrue(plugin.isEnabled());
        assertNotNull(VillagerOptimizer.getInstance());
        assertNotNull(VillagerOptimizer.config());
        assertNotNull(VillagerOptimizer.wrappers());
        assertNotNull(VillagerOptimizer.scheduling());
    }
}
