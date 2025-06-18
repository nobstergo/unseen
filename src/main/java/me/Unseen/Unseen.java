package me.Unseen;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.*;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Iterator;

public class Unseen extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Unseen is now enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Unseen is now disabled.");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joining = event.getPlayer();

        event.setJoinMessage(null);

        for (Player other : Bukkit.getOnlinePlayers()) {
            if (!other.equals(joining)) {
                if (!joining.getWorld().equals(other.getWorld())) continue;

                double distance = joining.getLocation().distance(other.getLocation());

                if (distance <= 20) {
                    joining.showPlayer(this, other);
                    other.showPlayer(this, joining);
                } else {
                    joining.hidePlayer(this, other);
                    other.hidePlayer(this, joining);
                }
            }
        }

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
