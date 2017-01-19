package me.fredde.BuildSubmission;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachmentInfo;

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

        if (isNew) addBuilder(player);
    }

    private void addBuilder(Player player) {
        int rank = 0;

        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String permission = pai.getPermission();
            if (permission.startsWith("bs.rank")) rank = Integer.valueOf(permission.substring(6));
        }

        builders.add(new Builder(settings.BUILDERS, player.getUniqueId(), rank, null));
    }
}
