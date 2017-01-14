package me.fredde.BuildSubmission;

import org.bukkit.ChatColor;

import java.util.ArrayList;

class Settings {
    String SUBMITTED;
    String APPROVED;
    String DENIED;
    String INVALID;
    String CANCEL;

    ArrayList ONLINE;
    ArrayList OFFLINE;

    Manager CHAT;
    Manager COMMANDS;
    Manager BUILDERS;

    String cc(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
