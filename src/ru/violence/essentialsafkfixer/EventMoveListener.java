package ru.violence.essentialsafkfixer;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

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
        Player player = event.getPlayer();
        if (!checkIgnore(player.getUniqueId())) return;
        User user = this.ess.getUser(player);
        user.updateActivityOnMove(true);
    }

    private boolean checkIgnore(UUID uniqueId) {
        if (!this.tempIgnore.add(uniqueId)) return false;
        Bukkit.getServer().getScheduler().runTaskLater(this.plugin, () -> this.tempIgnore.remove(uniqueId), 20);
        return true;
    }
}
