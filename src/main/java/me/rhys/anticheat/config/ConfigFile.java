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
public class ConfigFile {

    private FileConfiguration fileConfiguration;
    private File dataFile;

    public void setup(JavaPlugin plugin) {
        plugin.getDataFolder().mkdir();

        this.dataFile = new File("plugins/Monolith/Config.yml");

        if (!this.dataFile.exists()) {
            try {
                this.dataFile.createNewFile();
            } catch (IOException ignored) {
            }
        }

        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.dataFile);
    }

    public void writeDefaults() {
        if (!this.fileConfiguration.contains("Alert.Message")) this.fileConfiguration.set("Alert.Message", "&4Monolith &8> &c%PLAYER% &ffailed &c%CHECK%%TYPE% &7(&fVL:%VL%&7)");
        if (!this.fileConfiguration.contains("Alert.HoverDebug")) this.fileConfiguration.set("Alert.HoverDebug", true);

        if (!this.fileConfiguration.contains("Punishment.Enabled")) this.fileConfiguration.set("Punishment.Enabled", false);

        if (!this.fileConfiguration.contains("Punishment.MaxViolations")) this.fileConfiguration.set("Punishment.MaxViolations", 20);

        if (!this.fileConfiguration.contains("Punishment.Commands")) this.fileConfiguration.set("Punishment.Commands",
                Collections.singletonList("ban %PLAYER% Unfair Advantage."));

        if (!this.fileConfiguration.contains("Punishment.Messages")) this.fileConfiguration.set("Punishment.Messages",
                Arrays.asList(" ", "&c✗ Monolith has removed &7%PLAYER% &cfor cheating.", " "));

        if (!this.fileConfiguration.contains("Hider.Enabled")) this.fileConfiguration.set("Hider.Enabled", false);
        if (!this.fileConfiguration.contains("Hider.Name")) this.fileConfiguration.set("Hider.Name", "saturn");
        if (!this.fileConfiguration.contains("Hider.BlockTabComplete")) this.fileConfiguration.set("Hider.BlockTabComplete", true);
        if (!this.fileConfiguration.contains("Hider.BlockGeneralCommands")) this.fileConfiguration.set("Hider.BlockGeneralCommands", true);

        if (!this.fileConfiguration.contains("PacketOrderKicks")) this.fileConfiguration.set("PacketOrderKicks", true);

        saveData();
    }

    public void saveData() {
        try {
            this.fileConfiguration.save(this.dataFile);
        } catch (IOException ignored) {
        }
    }
}
