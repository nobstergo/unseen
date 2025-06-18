package me.Unseen;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.TabCompleteEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Unseen extends JavaPlugin implements Listener, TabCompleter {

    @Override
    public void onEnable() {
        Bukkit.getScheduler().cancelTasks(this);
        runVisibilityUpdater(getConfig().getInt("visibility-distance", 20));

        getCommand("unseen").setTabCompleter(this);

        Bukkit.getPluginManager().registerEvents(this, this);
        getLogger().info("Unseen is now enabled.");
    }

    @Override
    public void onDisable() {
        getLogger().info("Unseen is now disabled.");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Bukkit.getScheduler().cancelTasks(this); // Cancel previous tasks
        runVisibilityUpdater(getConfig().getInt("visibility-distance", 20));
    }


    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("unseen")) {
            if (args.length == 0) {
                sender.sendMessage(Component.text("Type /unseen help for command info.").color(NamedTextColor.RED));
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "reload":
                    if (!sender.hasPermission("unseen.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                        return true;
                    }
                    reloadConfig();
                    sender.sendMessage(Component.text("Config is reloaded successfully!").color(NamedTextColor.GREEN));
                    break;

                case "distance":
                    if (!sender.hasPermission("unseen.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                        return true;
                    }
                    if (args.length != 2) {
                        sender.sendMessage(Component.text("Usage: /unseen distance <number>").color(NamedTextColor.YELLOW));
                        return true;
                    }
                    try {
                        int distance = Integer.parseInt(args[1]);
                        getConfig().set("visibility-distance", distance);
                        saveConfig();
                        sender.sendMessage(Component.text("Visibility distance set to " + distance).color(NamedTextColor.GREEN));
                        sender.sendMessage(Component.text("Reload the plugin with /unseen reload to apply changes.").color(NamedTextColor.YELLOW));
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid number!").color(NamedTextColor.RED));
                    }
                    break;

                case "joinmessage":
                case "leavemessage":
                case "deathmessage":
                    if (!sender.hasPermission("unseen.admin")) {
                        sender.sendMessage(Component.text("You don't have permission to use this command.").color(NamedTextColor.RED));
                        return true;
                    }
                    String key = switch (args[0].toLowerCase()) {
                        case "joinmessage" -> "show-join-message";
                        case "leavemessage" -> "show-quit-message";
                        case "deathmessage" -> "show-death-message";
                        default -> null;
                    };
                    if (key != null) {
                        boolean current = getConfig().getBoolean(key, false);
                        getConfig().set(key, !current);
                        saveConfig();
                        sender.sendMessage(Component.text(key + " set to " + (!current)).color(NamedTextColor.DARK_GREEN));
                        sender.sendMessage(Component.text("Reload the plugin with /unseen reload to apply changes.").color(NamedTextColor.YELLOW));
                    }
                    break;

                case "help":
                    sender.sendMessage(Component.text("=== Unseen Plugin Commands ===").color(NamedTextColor.GOLD));
                    sender.sendMessage(Component.text("/unseen reload ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Reload the config file").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen distance <number> ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Set player visibility distance").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen joinmessage ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Toggle join messages").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen leavemessage ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Toggle leave messages").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen deathmessage ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Toggle death messages").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen info ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Plugin info and authors").color(NamedTextColor.GRAY)));
                    break;

                case "info":
                    String version = getDescription().getVersion();
                    sender.sendMessage(Component.text("Unseen Plugin v" + version).color(NamedTextColor.GOLD));
                    sender.sendMessage(Component.text("Author: nobstergo").color(NamedTextColor.BLUE));
                    sender.sendMessage(Component.text("Contributor: alprny - Idea, Testing Feedback").color(NamedTextColor.AQUA));
                    sender.sendMessage(Component.text("Plugin hides players in the tablist, social interactions, and chat autocomplete based on distance. Join/leave/death messages also toggleable.").color(NamedTextColor.DARK_GREEN));
                    sender.sendMessage(Component.text("Made with <3").color(NamedTextColor.LIGHT_PURPLE));
                    break;

                default:
                    sender.sendMessage(Component.text("Unknown argument! Look at /unseen help and try again.").color(NamedTextColor.RED));
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, String[] args) {
        if (!command.getName().equalsIgnoreCase("unseen")) return Collections.emptyList();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            if (sender.hasPermission("unseen.admin")) {
                completions.addAll(List.of("reload", "distance", "joinmessage", "leavemessage", "deathmessage", "help", "info"));
            } else {
                completions.add("info");
            }

            return completions.stream()
                    .filter(c -> c.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
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
        if (!getConfig().getBoolean("show-join-message", false)) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!getConfig().getBoolean("show-quit-message", false)) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!getConfig().getBoolean("show-death-message", false)) {
            event.setDeathMessage(null);
        }
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
