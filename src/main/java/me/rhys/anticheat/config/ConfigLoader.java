package me.rhys.anticheat.config;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.util.string.StringUtil;

public class ConfigLoader {

    public void loadConfig() {
        ConfigFile configFile = new ConfigFile();
        configFile.setup(Plugin.getServerInstance());
        configFile.writeDefaults();

        Plugin.getInstance().getConfigValues().setAlertMessage(StringUtil.convertColor(
                configFile.getFileConfiguration().getString("Alert.Message")));

        Plugin.getInstance().getConfigValues().setHoverDebug(
                configFile.getFileConfiguration().getBoolean("Alert.HoverDebug"));

        Plugin.getInstance().getConfigValues().setPunish(
                configFile.getFileConfiguration().getBoolean("Punishment.Enabled"));

        Plugin.getInstance().getConfigValues().setMaxViolations(
                configFile.getFileConfiguration().getInt("Punishment.MaxViolations"));

        configFile.getFileConfiguration().getStringList("Punishment.Commands").forEach(s ->
                Plugin.getInstance().getConfigValues().getPunishCommands().add(StringUtil.convertColor(s)));

        configFile.getFileConfiguration().getStringList("Punishment.Messages").forEach(s ->
                Plugin.getInstance().getConfigValues().getPunishMessages().add(StringUtil.convertColor(s)));

        Plugin.getInstance().getConfigValues().setHider(
                configFile.getFileConfiguration().getBoolean("Hider.Enabled"));

        Plugin.getInstance().getConfigValues().setHiderName(
                configFile.getFileConfiguration().getString("Hider.Name"));

        Plugin.getInstance().getConfigValues().setBlockTabComplete(
                configFile.getFileConfiguration().getBoolean("Hider.BlockTabComplete"));

        Plugin.getInstance().getConfigValues().setBlockGeneralCommands(
                configFile.getFileConfiguration().getBoolean("Hider.BlockGeneralCommands"));

        Plugin.getInstance().getConfigValues().setFaggotKick(configFile.getFileConfiguration()
                .getBoolean("PacketOrderKicks"));
    }
}
