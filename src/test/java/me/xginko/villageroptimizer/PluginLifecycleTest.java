package me.xginko.villageroptimizer;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
