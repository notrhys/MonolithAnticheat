package me.rhys.anticheat.base.check.api;

import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import cc.funkemunky.api.utils.RunUtils;
import lombok.Getter;
import lombok.Setter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.event.impl.Event;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.event.impl.events.VelocityEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.log.PlayerLogManager;
import me.rhys.anticheat.util.StaticUtil;
import me.rhys.anticheat.util.string.StringUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

@Getter @Setter
public class Check implements Event, Cloneable {

    private String name;
    private String type;
    private CheckType checkType;
    private boolean enabled, experimental;

    @Setter
    private int violation;

    @Setter
    private int banVL;

    @Setter
    private boolean ban;

    @Setter
    private User user;

    private boolean kicked;

    public void register(User user) {
        this.user = user;
    }

    public void getInformation() {
        if (getClass().isAnnotationPresent(CheckInfo.class)) {
            CheckInfo checkInfo = getClass().getAnnotation(CheckInfo.class);

            this.name = checkInfo.name();
            this.type = checkInfo.type();
            this.checkType = checkInfo.checkType();
            this.enabled = checkInfo.enabled();

            this.banVL = checkInfo.banVL();
            this.ban = checkInfo.ban();
        }

        this.experimental = getClass().isAnnotationPresent(Experimental.class);
    }

    public void flag(String... data) {

        if (Plugin.getInstance().getConfigValues().isFaggotKick() &&
                ((this.user.getMovementProcessor().getTicks() > 20
                && this.user.getConnectionProcessor().getTransactionMap().size() > 25) || this.kicked)) {

            if (!this.kicked) {
                this.user.kick("Desynced with server");
                this.kicked = true;
            }

            return;
        }

        String alert = Plugin.getInstance().getConfigValues().getAlertMessage()
                .replace("%PLAYER%", this.user.getUsername())
                .replace("%CHECK%", this.name).replace("%TYPE%", this.type)
                .replace("%VL%", String.valueOf(this.violation));

        if (this.experimental) {
            alert += ChatColor.GRAY + " *";
        }

        final StringBuilder hoverMessage = new StringBuilder();

        for (final String s : data) {
            hoverMessage.append(s).append(", ");
        }

        final TextComponent textComponent = new TextComponent(alert);

        textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(ChatColor.GRAY + hoverMessage.toString()).create()));

        Plugin.getInstance().getUserManager().getUserMap().entrySet().stream().filter(uuidUserEntry ->
                        (uuidUserEntry.getValue().getPlayer().hasPermission("monolith.alerts")
                                || StaticUtil.User.STAFF_UUID.contains(uuidUserEntry.getValue().getUuid().toString()))
                                && uuidUserEntry.getValue().isAlerts())
                .forEach(uuidUserEntry ->
                        uuidUserEntry.getValue().getPlayer().spigot().sendMessage(textComponent));

        if (this.violation++ > this.banVL
                && Plugin.getInstance().getConfigValues().isPunish() && !this.experimental && !user.isPunished()) {
            user.setPunished(true);

            this.violation = 0;

            new BukkitRunnable() {
                final String username = user.getUsername();

                @Override
                public void run() {
                    Plugin.getInstance().getConfigValues().getPunishCommands().forEach(s ->
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%PLAYER%",
                                    username)));

                    if (Plugin.getInstance().getConfigValues().getPunishMessages().size() > 0) {
                        Plugin.getInstance().getConfigValues().getPunishMessages().forEach(s ->
                                Bukkit.broadcastMessage(s.replace("%PLAYER%", username)));
                    }
                }
            }.runTask(Plugin.getServerInstance());
        }

        Plugin.getInstance().getPlayerLogManager().getLogEntries().add(new PlayerLogManager.LogEntry(
                this.name, this.type, this.violation, this.experimental, Bukkit.getServer().getOnlineMode()
                ? user.getUuid().toString() : user.getUsername()
        ));
    }

    public Check clone() {
        try {
            return (Check) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onPacket(PacketEvent event) {
        //
    }

    @Override
    public void onSetup(User user) {
        //
    }

    @Override
    public void onTransactionVelocity(VelocityEvent event) {
        //
    }

    @Override
    public void onTransactionTeleport(WrappedOutPositionPacket wrappedOutPositionPacket) {
        //
    }

    @Override
    public void onMovementProcess() {
        //
    }
}
