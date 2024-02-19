package me.rhys.anticheat;

import cc.funkemunky.api.Atlas;
import lombok.Getter;
import me.rhys.anticheat.base.check.StaticCheckManager;
import me.rhys.anticheat.base.event.api.EventManager;
import me.rhys.anticheat.base.nms.api.NmsManager;
import me.rhys.anticheat.base.protocol.PacketManager;
import me.rhys.anticheat.base.thread.ThreadManager;
import me.rhys.anticheat.base.tracker.PastLocationTracker;
import me.rhys.anticheat.base.transaction.TransactionRunnable;
import me.rhys.anticheat.base.user.UserManager;
import me.rhys.anticheat.command.CommandManager;
import me.rhys.anticheat.config.ConfigLoader;
import me.rhys.anticheat.config.ConfigValues;
import me.rhys.anticheat.filter.LogManager;
import me.rhys.anticheat.listeners.PlayerListener;
import me.rhys.anticheat.log.PlayerLogManager;
import me.rhys.anticheat.util.Manifest;
import me.rhys.anticheat.util.block.BlockUtil;
import me.rhys.anticheat.version.VersionHook;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public class Plugin extends JavaPlugin {

    @Getter private static Plugin instance;
    @Getter private static JavaPlugin serverInstance;

    private final EventManager eventManager = new EventManager();
    private UserManager userManager;
    private final ThreadManager threadManager = new ThreadManager();
    private NmsManager nmsManager;
    private final PastLocationTracker pastLocationTracker = new PastLocationTracker();

    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private TransactionRunnable transactionRunnable;
    private PacketManager packetManager;

    private final LogManager logManager = new LogManager();
    private final PlayerLogManager playerLogManager = new PlayerLogManager();

    private final ConfigValues configValues = new ConfigValues();
    private final ConfigLoader configLoader = new ConfigLoader();

    private final StaticCheckManager staticCheckManager = new StaticCheckManager();

    private final CommandManager commandManager = new CommandManager();

    private final Manifest manifest = new Manifest("1.3.4-BETA");

    private final ScheduledExecutorService monitorService = Executors.newSingleThreadScheduledExecutor();

    private long lastMainTick;
    private boolean hasMonitorSet;

    private boolean lagging;
    private int serverLagTicks;

    private final VersionHook versionHook = new VersionHook();

    private final String licenseKey = "LICENSE_KEY_HERE";

    @Override
    public void onEnable() {
        this.start(this);
    }

    @Override
    public void onDisable() {
        this.stop(this);
    }

    public void start(JavaPlugin plugin) {
        long start = System.currentTimeMillis();

        instance = this;
        serverInstance = plugin;

        this.staticCheckManager.setup();
        this.nmsManager = new NmsManager();
        this.userManager = new UserManager();

        Bukkit.getOnlinePlayers().forEach(this.userManager::add);

        this.versionHook.setup();
        plugin.getServer().getPluginManager().registerEvents(new PlayerListener(), plugin);
        this.packetManager = new PacketManager();
        this.transactionRunnable = new TransactionRunnable();
        this.pastLocationTracker.start();
        this.configLoader.loadConfig();
        new BlockUtil();

        this.executorService.scheduleAtFixedRate(() -> this.userManager.getUserMap().forEach((uuid, user) ->
                        user.getCheckManager().getChecks().forEach(check -> check.setViolation(0))),
                3L, 3L, TimeUnit.MINUTES);

        this.logManager.setupFilter();
        this.commandManager.setup();

        plugin.getServer().getConsoleSender().sendMessage(ChatColor.GREEN + String.format(
                "Loaded Monolith (%s) in ", this.manifest.getVersion())
                + (System.currentTimeMillis() - start) + "ms");

        this.runMonitor();
        this.playerLogManager.start();
    }

    public void stop(JavaPlugin plugin) {
        this.playerLogManager.shutdown();
        this.commandManager.unRegisterAll();
        this.monitorService.shutdownNow();

        Plugin.getInstance().getExecutorService().shutdownNow();
        Atlas.getInstance().getEventManager().unregisterAll(plugin);
    }

    private void runMonitor() {

        // run bukkit runnable to check main thread for any lag spikes

        new BukkitRunnable() {

            @Override
            public void run() {
                lastMainTick = System.currentTimeMillis();
                hasMonitorSet = true;
            }
        }.runTaskTimer(serverInstance, 0L, 0L);

        // do the checks on our other thread

        this.monitorService.scheduleAtFixedRate(() -> {

            if (!this.hasMonitorSet) return;

            final long now = System.currentTimeMillis();
            final long lastTick = (now - this.lastMainTick);
            final int toTicks = (int) (lastTick / 50.0);

            if (toTicks > 3) {

                if (this.serverLagTicks == 0) {
                    serverInstance.getLogger().warning("Server lag detected - " + toTicks + " " + lastTick);
                }

                this.serverLagTicks = 100;
            }

            this.lagging = this.serverLagTicks-- > 0;
        }, 60L, 60L, TimeUnit.MILLISECONDS);
    }
}
