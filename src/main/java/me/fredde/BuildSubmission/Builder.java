package me.fredde.BuildSubmission;

import org.bukkit.Location;

import java.util.UUID;

class Builder {
    private Manager manager;
    private UUID uuid;
    private int rank;
    private Location location;

    Builder(Manager manager, UUID uuid, int rank, Location location) {
        this.manager = manager;
        this.uuid = uuid;
        this.rank = rank;
        this.location = location;

        // Write to builders.yml.
        writeRank(rank);
        writeLocation(location);
    }

    UUID getUuid() {
        return uuid;
    }

    int getRank() {
        return rank;
    }

    void setRank(int rank) {
        this.rank = rank;
        writeRank(rank);
    }

    Location getLocation() {
        return location;
    }

    void setLocation(Location location) {
        this.location = location;
        writeLocation(location);
    }

    private void writeRank(int rank) {
        manager.write(uuid.toString() + ".rank", rank);
    }

    private void writeLocation(Location location) {
        if (location != null) {
            manager.write(uuid.toString() + ".world.name", location.getWorld().getName());
            manager.write(uuid.toString() + ".world.x", location.getX());
            manager.write(uuid.toString() + ".world.y", location.getY());
            manager.write(uuid.toString() + ".world.z", location.getZ());
        } else {
            manager.write(uuid.toString() + ".world", null);
        }
    }
}
