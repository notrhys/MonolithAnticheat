package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.math.ClickUtils;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "BadPackets", type = "G", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsG extends Check {

    private final List<Integer> delays = new ArrayList<>();
    private int movements;
    private double threshold;

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {

            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {
                this.movements++;
                break;
            }

            case Packet.Client.BLOCK_DIG: {

                WrappedInBlockDigPacket digPacket =
                        new WrappedInBlockDigPacket(event.getPacket(), getUser().getPlayer());

                if (getUser().getMovementProcessor().getTicks() < 60
                        || getUser().getPlayer().isFlying()
                        || getUser().getPlayer().getAllowFlight()) {
                    movements = 20;
                    return;
                }

                if (digPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {

                    if (getUser().isSword(getUser().getPlayer().getItemInHand())
                            && getUser().getPlayer().getItemInHand() != null) {
                        if (movements < 10) {
                            this.delays.add(movements);

                            if (this.delays.size() == 25) {
                                double std = ClickUtils.getStandardDeviation(this.delays);

                                if (std < 0.34) {
                                    if (threshold++ > 1) {
                                        flag("Blocking too consistent");
                                    }
                                } else {
                                    threshold -= Math.min(threshold, 0.5);
                                }

                                this.delays.clear();
                            }
                        }
                        this.movements = 0;
                    }
                }
                break;
            }
        }
    }
}