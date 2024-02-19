package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.math.ClickUtils;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "AutoClicker", type = "J", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerJ extends Check {

    private double threshold, lastSTD, lastDifference;
    private final List<Integer> cpsList = new ArrayList<>();
    private int movements;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 10 && !getUser().getClickProcessor().isDigging()) {

                cpsList.add(movements);

                if (cpsList.size() == 50) {

                    double std = ClickUtils.getStandardDeviation(cpsList);

                    double difference = Math.abs(std - this.lastSTD);

                    double stdDelta = Math.abs(difference - this.lastDifference);

                    if (stdDelta <= 0.005) {
                        if (++threshold > 6) {
                            flag("delta=" + stdDelta + " d="+difference + " s="+std);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.125);
                    }

                    this.lastDifference = difference;
                    this.lastSTD = std;

                    cpsList.clear();
                }
            }

            movements = 0;
        }

        if (event.isMovement()) {
            movements++;
        }
    }
}