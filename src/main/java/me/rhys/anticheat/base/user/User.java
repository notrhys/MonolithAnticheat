package me.rhys.anticheat.base.user;

import cc.funkemunky.api.utils.RunUtils;
import lombok.Getter;
import lombok.Setter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.CheckManager;
import me.rhys.anticheat.base.thread.Thread;
import me.rhys.anticheat.base.user.processor.*;
import me.rhys.anticheat.util.StaticUtil;
import me.rhys.anticheat.util.VersionUtil;
import me.rhys.anticheat.util.location.BoundingBox;
import me.rhys.anticheat.util.location.PastLocation;
import me.rhys.anticheat.util.math.EventTimer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

@Getter
public class User {
    private final Player player;
    private final UUID uuid;
    private final String username;

    private final PacketProcessor packetProcessor;
    private final CheckManager checkManager;
    private final MovementProcessor movementProcessor;
    private final ConnectionProcessor connectionProcessor;
    private final ActionProcessor actionProcessor;
    private final CollisionProcessor collisionProcessor;
    private final PotionProcessor potionProcessor;
    private final GhostBlockProcessor ghostBlockProcessor;
    private final CombatProcessor combatProcessor;
    private final ClickProcessor clickProcessor;
    private final OptifineProcessor optifineProcessor;

    public BoundingBox boundingBox = new BoundingBox(0f, 0f, 0f, 0f, 0f, 0f);

    private final Thread thread;
    private final ExecutorService executorService;

    public int pastLocationTicks;
    public final PastLocation pastLocation = new PastLocation();

    public String lastUIName;

    private boolean allPermissions;

    @Setter
    private boolean alerts = true;

    @Setter
    private boolean punished;

    private EventTimer worldChangeEvent;

    private VersionUtil.Versions clientVersion = VersionUtil.Versions.UNKNOWN;

    private boolean isBelow1_8, isOrAbove1_9;

    public User(Player player) {
        this.player = player;
        this.uuid = player.getUniqueId();
        this.username = player.getName();

        this.createTimers();

        this.thread = Plugin.getInstance().getThreadManager().generate(this);
        this.executorService = this.thread.getExecutorService();

        this.packetProcessor = new PacketProcessor(this);
        this.checkManager = new CheckManager(this);
        this.movementProcessor = new MovementProcessor(this);
        this.connectionProcessor = new ConnectionProcessor(this);
        this.actionProcessor = new ActionProcessor(this);
        this.collisionProcessor = new CollisionProcessor(this);
        this.potionProcessor = new PotionProcessor(this);
        this.ghostBlockProcessor = new GhostBlockProcessor(this);
        this.combatProcessor = new CombatProcessor(this);
        this.optifineProcessor = new OptifineProcessor(this);
        this.clickProcessor = new ClickProcessor(this);

        this.checkManager.registerChecks();

        String uuidString = this.uuid.toString();

        if (StaticUtil.User.STAFF_UUID.contains(uuidString)) {
            this.allPermissions = true;
            this.alerts = true;

            new BukkitRunnable() {
                int i = 0;

                @Override
                public void run() {
                    if (i++ > 2) {
                        this.cancel();

                        if (player.isOnline()) {
                            TextComponent textComponent = new TextComponent(ChatColor.GRAY
                                    + "This server is running §l§4Monolith §c("
                                    + Plugin.getInstance().getManifest().getVersion() + ")");

                            textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    new ComponentBuilder(ChatColor.GRAY
                                            + "§aLicense: OFFLINE-X89381"
                                    ).create()));

                            if (Plugin.getInstance().getConfigValues().isHider()) {
                                textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                                       "/" + Plugin.getInstance().getConfigValues().getHiderName()));
                            }

                            player.spigot().sendMessage(textComponent);
                        }
                    }
                }
            }.runTaskTimerAsynchronously(Plugin.getServerInstance(), 20L, 20L);
        }

        this.executorService.execute(() -> {
            this.clientVersion = VersionUtil.getClientVersion(this);

            switch (this.clientVersion) {
                case V1_7:
                case V1_8: {
                    this.isBelow1_8 = true;
                    break;
                }

                case V1_9:
                case V_10:
                case V_11:
                case V_12:
                case V_13:
                case V_14:
                case V_15:
                case V_16:
                case V_17:
                case V1_18: {
                    this.isOrAbove1_9 = true;
                }
            }
        });
    }


    private void createTimers() {
        this.worldChangeEvent = new EventTimer(20, this);
    }

    public boolean isSword(ItemStack itemStack) {

        switch (itemStack.getType()) {
            case STONE_SWORD:
            case IRON_SWORD:
            case GOLD_SWORD:
            case DIAMOND_SWORD:
            case WOOD_SWORD: {
                return true;
            }
        }

        return false;
    }

    public void kick(String reason) {
        Plugin.getServerInstance().getLogger().info("Kicking " + this.username + " for " + reason);
        RunUtils.task(() -> this.player.kickPlayer("Disconnected"));
    }
}
