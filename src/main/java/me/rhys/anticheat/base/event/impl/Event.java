package me.rhys.anticheat.base.event.impl;

import cc.funkemunky.api.tinyprotocol.packet.out.WrappedOutPositionPacket;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.event.impl.events.VelocityEvent;
import me.rhys.anticheat.base.user.User;

public interface Event {
    void onPacket(PacketEvent event);
    void onSetup(User user);
    void onTransactionVelocity(VelocityEvent event);
    void onTransactionTeleport(WrappedOutPositionPacket wrappedOutPositionPacket);
    void onMovementProcess();
}
