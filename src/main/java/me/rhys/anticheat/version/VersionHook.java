package me.rhys.anticheat.version;

import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.version.api.IVersion;
import me.rhys.anticheat.version.impl.ProtocolSupport;
import me.rhys.anticheat.version.impl.VanillaHook;
import me.rhys.anticheat.version.impl.ViaVersionHook;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

@Getter
public class VersionHook {
    private IVersion version = new VanillaHook();

    public void setup() {
        this.createHook();
        this.checkLater();
    }

    private void checkLater() {

        new BukkitRunnable() {
            int i = 0;

            @Override
            public void run() {

                if (i++ > 500) {
                    this.cancel();
                }

                if (version instanceof VanillaHook) {
                    createHook();
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimerAsynchronously(Plugin.getServerInstance(), 0L, 0L);
    }

    private void createHook() {
        if (this.isPluginEnabled("ViaVersion")) {
            this.version = new ViaVersionHook();

            Plugin.getServerInstance().getLogger().info("Hooked into ViaVersion");
        } else if (this.isPluginEnabled("ProtocolSupport")) {
            this.version = new ProtocolSupport();

            Plugin.getServerInstance().getLogger().info("Hooked into ProtocolSupport");
        }
    }

    private boolean isPluginEnabled(String name) {
        final org.bukkit.plugin.Plugin plugin = Bukkit.getPluginManager().getPlugin(name);

        return plugin != null && plugin.isEnabled();
    }
}
