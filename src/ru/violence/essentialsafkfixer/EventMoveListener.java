package ru.violence.essentialsafkfixer;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class EventMoveListener implements Listener {
    private final EssentialsAFKFixerPlugin plugin;
    private final Essentials ess;
    private final Set<UUID> tempIgnore = new HashSet<>();

    public EventMoveListener(EssentialsAFKFixerPlugin plugin, Essentials ess) {
        this.plugin = plugin;
        this.ess = ess;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        resetAFK(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        resetAFK((Player) entity);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDismount(EntityDismountEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        resetAFK((Player) entity);
    }

    private void resetAFK(Player player) {
        if (!checkIgnore(player.getUniqueId())) return;
        this.ess.getUser(player).updateActivityOnMove(true);
    }

    private boolean checkIgnore(UUID uniqueId) {
        if (!this.tempIgnore.add(uniqueId)) return false;
        Bukkit.getServer().getScheduler().runTaskLater(this.plugin, () -> this.tempIgnore.remove(uniqueId), 20);
        return true;
    }
}
