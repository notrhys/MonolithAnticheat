package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.math.ClickUtils;

import java.util.ArrayList;
import java.util.List;

@CheckInfo(name = "BadPackets", type = "F", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsF extends Check {

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

            case Packet.Client.BLOCK_PLACE: {
                if (getUser().getPlayer().getAllowFlight() || getUser().getMovementProcessor().getTicks() < 60) {
                    movements = 20;
                    return;
                }
                if (getUser().isSword(getUser().getPlayer().getItemInHand())
                        && getUser().getPlayer().getItemInHand() != null) {
                    if (movements < 10) {
                        this.delays.add(this.movements);

                        if (this.delays.size() == 25) {
                            double std = ClickUtils.getStandardDeviation(this.delays);

                            if (std < 0.34) {
                                if (threshold++ > 1) {
                                    flag( "Blocking too consistent");
                                }
                            } else {
                                threshold -= Math.min(threshold, 0.5);
                            }

                            this.delays.clear();
                        }

                    }
                    this.movements = 0;
                }
                break;
            }
        }
    }
}