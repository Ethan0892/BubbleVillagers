package me.xginko.villageroptimizer;

import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class MockBukkitTestBase {

    protected ServerMock server;
    protected VillagerOptimizer plugin;

    @BeforeEach
    void setUpMockBukkit() {
        server = MockBukkit.mock();
        plugin = MockBukkit.load(VillagerOptimizer.class);
    }

    @AfterEach
    void tearDownMockBukkit() {
        MockBukkit.unmock();
    }
}
