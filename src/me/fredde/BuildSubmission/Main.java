package me.fredde.BuildSubmission;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // If you change between offline and online mode (without Bungee forward) and the saved path is based on your
        // UUID there could possible be duplicates.
        if (!getServer().getOnlineMode())
            getLogger().info("You have offline-mode set to false. This could create issues with UUID's if not properly configured.");

        // Initialize and fetch from our files.
        Settings settings = new Settings();
        fetchChat(settings);
        fetchCommands(settings);

        // Fetch from builders.yml.
        List<Builder> builders = fetchBuilders(settings);

        getServer().getPluginManager().registerEvents(new Events(settings, builders), this);
        getCommand("bs").setExecutor(new Commands(this, settings, builders));
    }

    private void fetchChat(Settings settings) {
        Manager manager = new Manager("chat.yml");

        List<String> path = new ArrayList<>();
        path.add("SUBMITTED");
        path.add("APPROVED");
        path.add("DENIED");
        path.add("INVALID");
        path.add("CANCEL");

        for (String p : path) {
            if (manager.read(p) != null) continue;
            switch (p) {
                case "SUBMITTED":
                    manager.write(path.get(0), "&eBuild submitted.");
                    break;
                case "APPROVED":
                    manager.write(path.get(1), "&aCongrats, your build was approved!");
                    break;
                case "DENIED":
                    manager.write(path.get(2), "&cSorry, your build got denied.");
                    break;
                case "INVALID":
                    manager.write(path.get(3), "&cInvalid argument or command.");
                    break;
                case "CANCEL":
                    manager.write(path.get(4), "&eYour submission has been cancelled.");
                    break;
                default:
                    break;
            }
        }

        path.stream().filter(p -> !(manager.read(p) instanceof String)).forEach(p -> quit(p + " is invalid."));

        settings.CHAT = manager;
        settings.SUBMITTED = settings.cc((String) manager.read(path.get(0)));
        settings.APPROVED = settings.cc((String) manager.read(path.get(1)));
        settings.DENIED = settings.cc((String) manager.read(path.get(2)));
        settings.INVALID = settings.cc((String) manager.read(path.get(3)));
        settings.CANCEL = settings.cc((String) manager.read(path.get(4)));
    }

    private void fetchCommands(Settings settings) {
        Manager manager = new Manager("commands.yml");

        List<String> path = new ArrayList<>();
        path.add("ONLINE");
        path.add("OFFLINE");

        for (String p : path) {
            if (manager.read(p) != null) continue;
            switch (p) {
                case "ONLINE":
                    manager.write(path.get(0), Arrays.asList("{rank1} tell {player} YAY", "{rank2} tell {player} WOO"));
                    break;
                case "OFFLINE":
                    manager.write(path.get(1), Arrays.asList("{rank1} promote {player}", "{rank2} promote {player}"));
                    break;
                default:
                    break;
            }
        }

        path.stream().filter(p -> !(manager.read(p) instanceof List)).forEach(p -> quit(p + " is invalid."));

        settings.COMMANDS = manager;
        settings.ONLINE = (ArrayList) manager.read(path.get(0));
        settings.OFFLINE = (ArrayList) manager.read(path.get(1));
    }

    private List<Builder> fetchBuilders(Settings settings) {
        Manager manager = new Manager("builders.yml");
        List<Builder> builders = new ArrayList<>();

        for (String builder : manager.getKeys("")) {
            UUID uuid = UUID.fromString(builder);
            int rank = (int) manager.read(builder + ".rank");
            Location location = null;

            if (manager.read(builder + ".world") != null) {
                World world = getServer().getWorld((String) manager.read(builder + ".world.name"));
                double x = (double) manager.read(builder + ".world.x");
                double y = (double) manager.read(builder + ".world.y");
                double z = (double) manager.read(builder + ".world.z");
                location = new Location(world, x, y, z);
            }

            builders.add(new Builder(manager, uuid, rank, location));
        }

        settings.BUILDERS = manager;
        return builders;
    }

    private void quit(String message) {
        getLogger().warning(message);
        getServer().getPluginManager().disablePlugin(this);
    }
}
