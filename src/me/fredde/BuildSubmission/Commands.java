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
import java.util.stream.Collectors;

class Commands implements CommandExecutor {
    private Server server;
    private Settings settings;
    private List<Builder> builders;

    private final String ALREADY_SUBMITTED = "You've already submitted. Use '/bs cancel' to cancel it.";
    private final String NO_SUBMISSIONS = "You haven't submitted a build. Use '/bs submit' to do so.";
    private final String NONE_PENDING = "No pending submissions. Phew.";
    private final String NOT_UNDER_REVIEW = "{name} is not under review.";
    private final String REVIEW_APPROVED = "{name} approved.";
    private final String REVIEW_DENIED = "{name} denied.";
    private final String BUILDER_RESET = "{name} reset.";

    Commands(Main main, Settings settings, List<Builder> builders) {
        this.server = main.getServer();
        this.settings = settings;
        this.builders = builders;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) return false;
        Player player = (Player) commandSender;

        // End if no related permissions found.
        if (!player.hasPermission("bs.user") && !player.hasPermission("bs.admin"))
            return neutral(player, "Lacking permissions.");

        // End if no arguments provided.
        if (strings.length == 0) return error(player, "No arguments.");

        if (player.hasPermission("bs.user")) {

            // End if entry not found in builders.yml.
            Builder builder = getBuilder(player);
            if (builder == null) return error(player, "Internal errors, contact an admin.");

            if (strings[0].equalsIgnoreCase("submit")) {

                // Submit a build.
                if (builder.getLocation() != null) return neutral(player, ALREADY_SUBMITTED);

                builder.setLocation(player.getLocation());
                broadcast("&e" + player.getName() + " submitted a build.", "bs.admin");
                player.sendMessage(settings.SUBMITTED);

            } else if (strings[0].equalsIgnoreCase("cancel")) {

                // Cancel a submission.
                if (builder.getLocation() == null) return neutral(player, NO_SUBMISSIONS);

                builder.setLocation(null);
                broadcast("&e" + player.getName() + " cancelled his/her build.", "bs.admin");
                player.sendMessage(settings.CANCEL);

            }
        }

        if (player.hasPermission("bs.admin")) {

            if (strings[0].equalsIgnoreCase("list")) {

                // View all submissions.
                if (getSubmissions().size() == 0) return neutral(player, NONE_PENDING);

                for (Builder builder : getSubmissions()) {
                    String name = server.getOfflinePlayer(builder.getUuid()).getName();
                    player.sendMessage(settings.cc("&e" + name));
                    player.sendMessage(settings.cc("&a/bs review " + name));
                    player.sendMessage(settings.cc("&6--"));
                }

            } else if (strings.length == 2) {

                // Fetch offline player.
                OfflinePlayer offlinePlayer = getOfflinePlayer(strings[1]);
                if (offlinePlayer == null) return neutral(player, "Player not found.");
                String name = offlinePlayer.getName();

                // End if entry not found in builders.yml.
                Builder builder = getBuilder(offlinePlayer.getPlayer());
                if (builder == null) return error(player, "Internal errors, contact an admin.");

                if (strings[0].equalsIgnoreCase("review")) {

                    // Review a submission.
                    if (builder.getLocation() == null) return neutral(player, NOT_UNDER_REVIEW.replace("{name}", name));

                    player.teleport(builder.getLocation());
                    player.sendMessage(settings.cc("&eTeleported to " + name + "."));

                } else if (strings[0].equalsIgnoreCase("rank")) {

                    // View a player's rank.
                    player.sendMessage(settings.cc("&e" + name + " -> rank: " + builder.getRank()));

                } else if (strings[0].equalsIgnoreCase("approve")) {

                    // Approve a submission.
                    if (builder.getLocation() == null) return neutral(player, NOT_UNDER_REVIEW.replace("{name}", name));

                    builder.setRank(builder.getRank() + 1);
                    builder.setLocation(null);
                    String rank = "{rank" + builder.getRank() + "}";

                    // TODO: Combine the command loops below.

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

                    if (offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(settings.APPROVED);
                    return positive(player, REVIEW_APPROVED.replace("{name}", name));

                } else if (strings[0].equalsIgnoreCase("deny")) {

                    // Deny a submission.
                    if (builder.getLocation() == null) return neutral(player, NOT_UNDER_REVIEW.replace("{name}", name));
                    builder.setLocation(null);

                    if (offlinePlayer.isOnline()) offlinePlayer.getPlayer().sendMessage(settings.DENIED);
                    return positive(player, REVIEW_DENIED.replace("{name}", name));

                } else if (strings[0].equalsIgnoreCase("reset")) {

                    // Reset a player's rank.
                    builder.setRank(0);
                    builder.setLocation(null);

                    return positive(player, BUILDER_RESET.replace("{name}", name));

                }
            }
        }

        return true;
    }

    private Builder getBuilder(Player player) {
        Builder object = null;

        Optional<Builder> fetch = builders.stream().filter(p -> p.getUuid().equals(player.getUniqueId())).findFirst();
        if (fetch.isPresent()) object = fetch.get();

        return object;
    }

    private OfflinePlayer getOfflinePlayer(String name) {
        OfflinePlayer object = null;

        Optional<OfflinePlayer> fetch = Arrays.stream(server.getOfflinePlayers()).filter(o -> o.getName().equalsIgnoreCase(name)).findFirst();
        if (fetch.isPresent()) object = fetch.get();

        return object;
    }

    private List<Builder> getSubmissions() {
        return builders.stream().filter(builder -> builder.getLocation() != null).collect(Collectors.toList());
    }

    private void broadcast(String message, String permission) {
        server.broadcast(settings.cc(message), permission);
    }

    private boolean neutral(Player player, String message) {
        player.sendMessage(settings.cc("&e" + message));
        return true;
    }

    private boolean positive(Player player, String message) {
        player.sendMessage(settings.cc("&a" + message));
        return true;
    }

    private boolean error(Player player, String message) {
        player.sendMessage(settings.cc("&c" + message));
        return false;
    }
}
