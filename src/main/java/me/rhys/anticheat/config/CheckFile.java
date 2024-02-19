package me.rhys.anticheat.config;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

@SuppressWarnings("ResultOfMethodCallIgnored")
@Getter
public class CheckFile {

    private FileConfiguration fileConfiguration;
    private File dataFile;

    public void setup(JavaPlugin plugin) {
        plugin.getDataFolder().mkdir();

        this.dataFile = new File("plugins/Monolith/Checks.yml");

        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            } catch (IOException ignored) {
            }
        }

        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.dataFile);
    }

    public void saveData() {
        try {
            this.fileConfiguration.save(this.dataFile);
        } catch (IOException ignored) {
        }
    }
}
