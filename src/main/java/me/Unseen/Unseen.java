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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class Unseen extends JavaPlugin implements Listener, TabCompleter {

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            new PacketHider(this); // only runs if ProtocolLib exists
        } else {
            getLogger().warning("ProtocolLib not found! Tab list/social menu hiding will not work.");
        }
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
                    sender.sendMessage(
                        Component.text("[Unseen] ", NamedTextColor.GOLD)
                            .append(Component.text("Type /unseen help for command info.", NamedTextColor.RED))
                    );
                    return true;
                }

            switch (args[0].toLowerCase()) {
            case "distance":
                if (!sender.hasPermission("unseen.admin")) {
                    sender.sendMessage(
                        Component.text("[Unseen] ", NamedTextColor.GOLD)
                            .append(Component.text("You don't have permission to use this command.", NamedTextColor.RED))
                    );
                    return true;
                }
                if (args.length != 2) {
                    sender.sendMessage(
                        Component.text("[Unseen] ", NamedTextColor.GOLD)
                            .append(Component.text("Usage: /unseen distance <number>", NamedTextColor.RED))
                    );
                    return true;
                }
                try {
                    int distance = Integer.parseInt(args[1]);
                    getConfig().set("visibility-distance", distance);
                    saveConfig();
                    Bukkit.getScheduler().cancelTasks(this);
                    runVisibilityUpdater(distance);

                    sender.sendMessage(
                        Component.text("[Unseen] ", NamedTextColor.GOLD)
                            .append(Component.text("Visibility distance set to " + distance, NamedTextColor.GREEN))
                    );
                    sender.sendMessage(
                        Component.text("[Unseen] ", NamedTextColor.GOLD)
                            .append(Component.text("Changes applied!", NamedTextColor.YELLOW))
                    );
                } catch (NumberFormatException e) {
                    sender.sendMessage(
                        Component.text("[Unseen] ", NamedTextColor.GOLD)
                            .append(Component.text("Invalid distance!", NamedTextColor.RED))
                    );
                }
                break;

                case "joinmessage":
                case "leavemessage":
                case "deathmessage":
                    if (!sender.hasPermission("unseen.admin")) {
                        sender.sendMessage(
                            Component.text("[Unseen] ", NamedTextColor.GOLD)
                                .append(Component.text("You don't have permission to use this command.", NamedTextColor.RED))
                        );
                        return true;
                    }
                    String configKey = switch (args[0].toLowerCase()) {
                        case "joinmessage" -> "show-join-message";
                        case "leavemessage" -> "show-quit-message";
                        case "deathmessage" -> "show-death-message";
                        default -> null;
                    };
                    String displayName = switch (args[0].toLowerCase()) {
                        case "joinmessage" -> "Join messages";
                        case "leavemessage" -> "Quit messages";
                        case "deathmessage" -> "Death messages";
                        default -> null;
                    };

                    if (configKey != null) {
                        boolean current = getConfig().getBoolean(configKey, false);
                        boolean newValue = !current;
                        getConfig().set(configKey, newValue);
                        saveConfig();

                        NamedTextColor statusColor = newValue ? NamedTextColor.GREEN : NamedTextColor.RED;

                        sender.sendMessage(
                            Component.text("[Unseen] ", NamedTextColor.GOLD)
                                .append(Component.text(displayName + " " + (newValue ? "enabled" : "disabled"), statusColor))
                        );

                        if (configKey.equals("show-join-message") || configKey.equals("show-quit-message") || configKey.equals("show-death-message")) {
                            reloadConfig();
                        }
                    }
                    break;

            case "reload":
                if (!sender.hasPermission("unseen.admin")) {
                    sender.sendMessage(Component.text("[Unseen] You don't have permission to use this command.")
                            .color(NamedTextColor.RED));
                    return true;
                }
                reloadConfig();
                sender.sendMessage(
                    Component.text("[Unseen] ", NamedTextColor.GOLD)
                        .append(Component.text("Config reloaded successfully!", NamedTextColor.GREEN))
                );
                break;

                case "help":
                    String version = getDescription().getVersion();
                    sender.sendMessage(Component.text("====== Unseen v" + version + " ======").color(NamedTextColor.GOLD));
                    sender.sendMessage(Component.text("/unseen distance <number> ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Set player visibility distance").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen joinmessage ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Toggle join messages").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen leavemessage ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Toggle leave messages").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen deathmessage ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Toggle death messages").color(NamedTextColor.GRAY)));
                    sender.sendMessage(Component.text("/unseen reload ").color(NamedTextColor.YELLOW)
                            .append(Component.text("- Reload the config file").color(NamedTextColor.GRAY)));
                    break;


                default:
                sender.sendMessage(
                    Component.text("[Unseen] ", NamedTextColor.GOLD)
                        .append(Component.text("Unknown argument! Look at /unseen help and try again.", NamedTextColor.RED))
                );
                    break;
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias,
            String[] args) {
        if (!command.getName().equalsIgnoreCase("unseen"))
            return Collections.emptyList();

        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            if (sender.hasPermission("unseen.admin")) {
                completions.addAll(
                        List.of("distance", "joinmessage", "leavemessage", "deathmessage", "reload", "help"));
            }

            return completions.stream()
                    .filter(c -> {
                        Player target = Bukkit.getPlayerExact(c);
                        if (target == null) return true;

                        // If sender is OP, show all names
                        if (sender.isOp()) return true;

                        // Hide OPs from normal players
                        return !target.isOp();
                    })
                    .filter(c -> c.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }

        return Collections.emptyList();
    }

    private void runVisibilityUpdater(int visibilityDistance) {
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player p1 : Bukkit.getOnlinePlayers()) {
                for (Player p2 : Bukkit.getOnlinePlayers()) {
                    if (p1.equals(p2))
                        continue;
                    if (!p1.getWorld().equals(p2.getWorld()))
                        continue;

                    // If p1 is OP, always show everyone
                    if (p1.isOp()) {
                        p1.showPlayer(this, p2);
                        continue;
                    }

                    // If p2 is OP, hide them from normal players
                    if (p2.isOp()) {
                        p1.hidePlayer(this, p2);
                        continue;
                    }


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
