package me.rhys.anticheat.base.user.processor;

import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.StreamUtil;

@Getter
public class PacketProcessor {
    private final User user;

    public PacketProcessor(User user) {
        this.user = user;
    }

    public void handlePacket(String type, Object packet, long now) {

        this.user.getExecutorService().execute(() -> {
            PacketEvent packetEvent = new PacketEvent(now, type, packet);

            if (!Plugin.getInstance().isLagging()) {
                StreamUtil.getFiltered(this.user.getCheckManager().getChecks(), Check::isEnabled).forEach(check ->
                        check.onPacket(packetEvent));
            }

            this.user.getCollisionProcessor().handle(type, packet, now);
            this.user.getMovementProcessor().handle(type, packet, now);
            this.user.getConnectionProcessor().handle(type, packet, now);
            this.user.getActionProcessor().handle(type, packet, now);
            this.user.getPotionProcessor().handle(type, packet, now);
            this.user.getGhostBlockProcessor().handle(type, packet, now);
            this.user.getCombatProcessor().handle(type, packet, now);
            this.user.getOptifineProcessor().handle(type, packet, now);
            this.user.getClickProcessor().handle(type, packet, now);
        });
    }
}
