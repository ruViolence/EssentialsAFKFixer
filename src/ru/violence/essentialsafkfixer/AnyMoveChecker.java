package ru.violence.essentialsafkfixer;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import lombok.RequiredArgsConstructor;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class AnyMoveChecker implements Listener {
    private final Essentials ess;
    private final Map<UUID, LookupState> afkPlayers = new HashMap<>();
    private boolean checking = false;

    public AnyMoveChecker(EssentialsAFKFixerPlugin plugin, Essentials ess) {
        this.ess = ess;
        // Fill on plugin start
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ess.getUser(player).isAfk()) {
                this.afkPlayers.put(player.getUniqueId(), new LookupState(player, player.getLocation()));
            }
        }
        // Start checking task
        Bukkit.getScheduler().runTaskTimer(plugin, new AFKCheckTask(), 0, 5 * 20);
    }

    @EventHandler(ignoreCancelled = true)
    public void onAfkStatusChange(AfkStatusChangeEvent event) {
        User user = (User) event.getAffected();
        UUID uniqueId = user.getBase().getUniqueId();

        if (event.getValue()/* New AFK state is true */) {
            Player player = user.getBase().getPlayer();
            this.afkPlayers.put(uniqueId, new LookupState(player, player.getLocation()));
        } else if (!this.checking) {
            this.afkPlayers.remove(uniqueId);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.afkPlayers.remove(event.getPlayer().getUniqueId());
    }

    public class AFKCheckTask implements Runnable {
        @Override
        public void run() {
            try {
                AnyMoveChecker.this.checking = true;
                for (Iterator<LookupState> iterator = AnyMoveChecker.this.afkPlayers.values().iterator(); iterator.hasNext(); ) {
                    LookupState state = iterator.next();

                    // Check if current location is not equals old location
                    if (!state.player.getLocation().equals(state.location)) {
                        User user = AnyMoveChecker.this.ess.getUser(state.player);
                        user.updateActivityOnMove(true);

                        iterator.remove();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                AnyMoveChecker.this.checking = false;
            }
        }
    }

    @RequiredArgsConstructor
    private static class LookupState {
        private final Player player;
        private final Location location;
    }
}
