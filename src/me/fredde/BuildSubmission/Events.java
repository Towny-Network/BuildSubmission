package me.fredde.BuildSubmission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
import java.util.UUID;

class Events implements Listener {
    private Settings settings;
    private List<Builder> builders;

    Events(Settings settings, List<Builder> builders) {
        this.settings = settings;
        this.builders = builders;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        boolean isNew = !builders.stream().filter(p -> p.getUuid().equals(uuid)).findFirst().isPresent();

        if (isNew) {
            if (!hasRankPermission(player)) builders.add(new Builder(settings.BUILDERS, uuid, 0, null));
        }
    }

    private boolean hasRankPermission(Player player) {
        boolean state = false;
        int highestRank = 0;

        // TODO: Combine the command loop below.

        for (Object o : settings.ONLINE) {
            String cmd = o.toString();
            cmd = cmd.trim();

            int i = cmd.indexOf("}");

            if (i > 0) {
                // TODO: Assuming that it's always index 5 which could be fetched dynamically instead.
                String rank = cmd.substring(5, i - 1);
                highestRank = Integer.valueOf(rank);
            }
        }

        for (Object o : settings.OFFLINE) {
            String cmd = o.toString();
            cmd = cmd.trim();

            int i = cmd.indexOf("}");

            if (i > 0) {
                // TODO: Assuming that it's always index 5 which could be fetched dynamically instead.
                String rank = cmd.substring(5, i - 1);
                int temp = Integer.valueOf(rank);
                if (temp > highestRank) highestRank = temp;
            }
        }

        Integer rank = null;

        for (int i = 0; i <= highestRank; i++) {
            if (player.hasPermission("bs.rank" + i)) rank = i;
        }

        if (rank != null) {
            builders.add(new Builder(settings.BUILDERS, player.getUniqueId(), rank, null));
            state = true;
        }

        return state;
    }
}
