package ru.violence.essentialsafkfixer;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import lombok.RequiredArgsConstructor;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.spigotmc.event.entity.EntityDismountEvent;
import org.spigotmc.event.entity.EntityMountEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class VehicleMoveListener implements Listener {
    private final Essentials ess;
    private final Map<UUID, LookupState> lookupStates = new HashMap<>();
    private boolean checking = false;

    public VehicleMoveListener(EssentialsAFKFixerPlugin plugin, Essentials ess) {
        this.ess = ess;
        // Fill on plugin start
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getVehicle() != null) {
                addToLookup(player);
            }
        }
        // Start checking task
        Bukkit.getScheduler().runTaskTimer(plugin, new MoveCheckTask(), 0, 5 * 20);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAfkStatusChange(AfkStatusChangeEvent event) {
        User user = (User) event.getAffected();
        UUID uniqueId = user.getBase().getUniqueId();

        if (event.getValue()/* New AFK state is true */) {
            addToLookup(user.getBase().getPlayer());
        } else {
            removeFromLookup(uniqueId);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityMount(EntityMountEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) return;
        addToLookup((Player) entity);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDismount(EntityDismountEvent event) {
        removeFromLookup(event.getEntity().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        removeFromLookup(event.getPlayer().getUniqueId());
    }

    private void addToLookup(Player player) {
        this.lookupStates.put(player.getUniqueId(), new LookupState(player, player.getLocation()));
    }

    private void removeFromLookup(UUID uniqueId) {
        if (!this.checking) this.lookupStates.remove(uniqueId);
    }

    public class MoveCheckTask implements Runnable {
        @Override
        public void run() {
            try {
                VehicleMoveListener.this.checking = true;
                for (Iterator<LookupState> iter = VehicleMoveListener.this.lookupStates.values().iterator(); iter.hasNext(); ) {
                    LookupState state = iter.next();

                    // Check if current location is not equals old location
                    if (!state.player.getLocation().equals(state.location)) {
                        VehicleMoveListener.this.ess.getUser(state.player).updateActivityOnMove(true);
                        iter.remove();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                VehicleMoveListener.this.checking = false;
            }
        }
    }

    @RequiredArgsConstructor
    private static class LookupState {
        private final Player player;
        private final Location location;
    }
}
