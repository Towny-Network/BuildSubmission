package me.fredde.BuildSubmission;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

class Commands implements CommandExecutor {
    private Server server;
    private Settings settings;
    private List<Builder> builders;

    Commands(Main main, Settings settings, List<Builder> builders) {
        this.server = main.getServer();
        this.settings = settings;
        this.builders = builders;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;
        if (!player.hasPermission("bs.user") || !player.hasPermission("bs.admin"))
            return error(player, "Lacking permissions.");
        if (strings.length == 0) return error(player, "No arguments.");

        if (player.hasPermission("bs.user")) {
            UUID uuid = player.getUniqueId();
            Optional<Builder> optional = builders.stream().filter(p -> p.getUuid().equals(uuid)).findFirst();
            if (!optional.isPresent()) return error(player, "Internal errors, contact an admin.");
            Builder builder = optional.get();

            if (strings[0].equalsIgnoreCase("submit")) {
                if (builder.getLocation() != null) return error(player, "You've already submitted.");

                builder.setLocation(player.getLocation());
                server.broadcast(settings.cc("&e" + player.getName() + " submitted a build."), "bs.admin");
                player.sendMessage(settings.SUBMITTED);
            } else if (strings[0].equalsIgnoreCase("cancel")) {
                if (builder.getLocation() == null) return error(player, "You haven't submitted a build.");

                builder.setLocation(null);
                server.broadcast(settings.cc("&e" + player.getName() + " cancelled his/her build."), "bs.admin");
                player.sendMessage(settings.CANCEL);
            }
        }

        if (player.hasPermission("bs.admin")) {
            if (strings[0].equalsIgnoreCase("list")) {
                builders.stream().filter(builder -> builder.getLocation() != null).forEach(builder -> {
                    String name = server.getOfflinePlayer(builder.getUuid()).getName();
                    player.sendMessage(settings.cc("&e" + name));
                    player.sendMessage(settings.cc("&a/bs review " + name));
                    player.sendMessage(settings.cc("&6--"));
                });
            } else if (strings.length == 2) {
                Optional<OfflinePlayer> offline = Arrays.stream(server.getOfflinePlayers()).filter(o -> o.getName().equalsIgnoreCase(strings[1])).findFirst();
                if (!offline.isPresent()) return error(player, "Player not found.");
                OfflinePlayer offlinePlayer = offline.get();
                UUID uuid = offlinePlayer.getUniqueId();
                String name = offlinePlayer.getName();
                Optional<Builder> optional = builders.stream().filter(p -> p.getUuid().equals(uuid)).findFirst();
                if (!optional.isPresent()) return error(player, "Internal errors, contact the author.");
                Builder builder = optional.get();

                if (strings[0].equalsIgnoreCase("review")) {
                    if (builder.getLocation() == null) return error(player, name + " is not under review.");
                    player.teleport(builder.getLocation());
                    player.sendMessage(settings.cc("&eTeleported to " + name + "."));
                } else if (strings[0].equalsIgnoreCase("rank")) {
                    player.sendMessage(settings.cc("&e" + name + " -> rank: " + builder.getRank()));
                } else if (strings[0].equalsIgnoreCase("approve")) {
                    if (builder.getLocation() == null) return error(player, name + " is not under review.");
                    builder.setRank(builder.getRank() + 1);
                    builder.setLocation(null);
                    String rank = "{rank" + builder.getRank() + "}";

                    for (Object o : settings.ONLINE) {
                        String cmd = o.toString();
                        if (cmd.contains(rank)) {
                            cmd = cmd.replace(rank, "");
                            cmd = cmd.replace("{player}", name);
                            cmd = cmd.trim();
                            server.dispatchCommand(server.getConsoleSender(), cmd);
                        }
                    }

                    for (Object o : settings.OFFLINE) {
                        String cmd = o.toString();
                        if (cmd.contains(rank)) {
                            cmd = cmd.replace(rank, "");
                            cmd = cmd.replace("{player}", name);
                            cmd = cmd.trim();
                            server.dispatchCommand(server.getConsoleSender(), cmd);
                        }
                    }

                    player.sendMessage(settings.cc("&e" + name + " approved."));
                    if (offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(settings.APPROVED);
                } else if (strings[0].equalsIgnoreCase("deny")) {
                    if (builder.getLocation() == null) return error(player, name + " is not under review.");
                    builder.setLocation(null);
                    player.sendMessage(settings.cc("&e" + name + " denied."));
                    if (offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(settings.DENIED);
                } else if (strings[0].equalsIgnoreCase("reset")) {
                    builder.setRank(0);
                    builder.setLocation(null);
                    player.sendMessage(settings.cc("&e" + name + " reset."));
                }
            }
        }

        return true;
    }

    private boolean error(Player player, String message) {
        player.sendMessage(settings.cc("&c" + message));
        return false;
    }
}
