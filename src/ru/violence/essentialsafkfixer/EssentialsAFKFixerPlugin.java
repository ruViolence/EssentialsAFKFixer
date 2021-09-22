package ru.violence.essentialsafkfixer;

import com.earth2me.essentials.Essentials;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class EssentialsAFKFixerPlugin extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        Essentials ess = (Essentials) getServer().getPluginManager().getPlugin("Essentials");
        getServer().getPluginManager().registerEvents(new EventMoveListener(this, ess), this);
        getServer().getPluginManager().registerEvents(new AnyMoveChecker(this, ess), this);
    }
}
