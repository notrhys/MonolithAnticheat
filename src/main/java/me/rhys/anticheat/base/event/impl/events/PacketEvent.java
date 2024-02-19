package me.rhys.anticheat.base.event.impl.events;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.Getter;
import me.rhys.anticheat.Plugin;

@Getter
public class PacketEvent {
    private final long now;
    private final String type;
    private final Object packet;

    private boolean movement;
    private boolean combat;

    public PacketEvent(long now, String type, Object packet) {
        this.now = now;
        this.type = type;
        this.packet = packet;

        switch (this.type) {

            case Packet.Client.USE_ENTITY: {
                this.combat = true;
                break;
            }

            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {
                this.movement = true;
                break;
            }
        }
    }
}
