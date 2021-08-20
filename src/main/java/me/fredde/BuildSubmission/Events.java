package me.fredde.BuildSubmission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.List;
import java.util.Optional;
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
            addBuilder(player);
        } else {
            checkBuilder(player);
        }
    }

    private void addBuilder(Player player) {
        int rank = getRankFromPermission(player);
        builders.add(new Builder(settings.BUILDERS, player.getUniqueId(), rank, null));
    }

    private void checkBuilder(Player player) {
        int rank = getRankFromPermission(player);
        UUID uuid = player.getUniqueId();
        Optional<Builder> optional = builders.stream().filter(p -> p.getUuid().equals(uuid)).findFirst();

        if (optional.isPresent()) {
            Builder builder = optional.get();
            if (rank == 0 && player.hasPermission("bs.rank0")) {
                builder.setRank(0);
            } else if (rank > 0) {
                builder.setRank(rank);
            }
        }
    }

    private int getRankFromPermission(Player player) {
        int rank = 0;

        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();

            // TODO: Check if temp is instance of Integer.
            if (permission.startsWith("bs.rank") && permission.length() > 7) {
                int temp = Integer.valueOf(permission.substring(7));
                if (temp > rank) rank = temp;
            }
        }

        return rank;
    }
}
