package me.fredde.BuildSubmission;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Set;

class Manager {
    private String path;
    private FileConfiguration configuration;
    private File file;

    Manager(String path) {
        this.path = path;
        init();
    }

    private void init() {
        // TODO: Avoid static path below.
        String root = "plugins/BuildSubmission";
        file = new File(root, path);

        if (!file.exists()) {
            if (file.getParentFile().mkdirs()) {
                try {
                    if (file.createNewFile()) {
                        configuration = YamlConfiguration.loadConfiguration(file);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        configuration = YamlConfiguration.loadConfiguration(file);
    }

    boolean write(String path, Object value) {
        boolean state = false;

        configuration.set(path, value);
        try {
            configuration.save(file);
            state = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return state;
    }

    Object read(String path) {
        return configuration.get(path);
    }

    Set<String> getKeys(String path) {
        return configuration.getConfigurationSection(path).getKeys(false);
    }
}
