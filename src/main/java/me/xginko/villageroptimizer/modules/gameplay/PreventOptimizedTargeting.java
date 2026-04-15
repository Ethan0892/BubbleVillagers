package me.xginko.villageroptimizer.modules.gameplay;

import me.xginko.villageroptimizer.modules.VillagerOptimizerModule;
import me.xginko.villageroptimizer.wrapper.WrappedVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityTargetEvent;

public class PreventOptimizedTargeting extends VillagerOptimizerModule implements Listener {

    public PreventOptimizedTargeting() {
        super("gameplay.prevent-entities-from-targeting-optimized");
    }

    @Override
    public void enable() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @Override
    public boolean shouldEnable() {
        return config.getBoolean(configPath + ".enable", true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onTarget(EntityTargetEvent event) {
        final Entity target = event.getTarget();
        if (
                target != null
                && target.getType() == EntityType.VILLAGER
                && wrapperCache.get((Villager) target, WrappedVillager::new).isOptimized()
        ) {
            event.setTarget(null);
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityTargetVillager(com.destroystokyo.paper.event.entity.EntityPathfindEvent event) {
        final Entity target = event.getTargetEntity();
        if (
                target != null
                && target.getType() == EntityType.VILLAGER
                && wrapperCache.get((Villager) target, WrappedVillager::new).isOptimized()
        ) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    private void onEntityAttackVillager(EntityDamageByEntityEvent event) {
        if (
                event.getEntityType() == EntityType.VILLAGER
                && event.getDamager() instanceof Mob
                && wrapperCache.get((Villager) event.getEntity(), WrappedVillager::new).isOptimized()
        ) {
            ((Mob) event.getDamager()).setTarget(null);
        }
    }
 }
