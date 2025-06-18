package me.Unseen;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

public class Unseen extends JavaPlugin implements Listener {

    private final int visibilityDistance = 20;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        int visibilityDistance = getConfig().getInt("visibility-distance", 20);

        runVisibilityUpdater(visibilityDistance);

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Unseen is now enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Unseen is now disabled.");
    }

    private void runVisibilityUpdater(int visibilityDistance) {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                for (Player p2 : Bukkit.getOnlinePlayers()) {
                    if (p1.equals(p2)) continue;
                    if (!p1.getWorld().equals(p2.getWorld())) continue;

                    double distance = p1.getLocation().distance(p2.getLocation());

                    if (distance <= visibilityDistance) {
                        p1.showPlayer(this, p2);
                    } else {
                        p1.hidePlayer(this, p2);
                    }
                }
            }
        }, 0L, 40L);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);
    }

    @EventHandler
    public void onTabComplete(TabCompleteEvent event) {
        Iterator<String> it = event.getCompletions().iterator();
        while (it.hasNext()) {
            String name = it.next();
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equalsIgnoreCase(name)) {
                    it.remove();
                }
            }
        }
    }
}
