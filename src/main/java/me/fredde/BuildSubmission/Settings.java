package me.fredde.BuildSubmission;

import org.bukkit.ChatColor;

import java.util.List;

class Settings {
    String SUBMITTED;
    String APPROVED;
    String DENIED;
    String INVALID;
    String CANCEL;

    List ONLINE;
    List OFFLINE;

    Manager CHAT;
    Manager COMMANDS;
    Manager BUILDERS;

    String cc(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
