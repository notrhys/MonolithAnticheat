package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.math.ClickUtils;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "AutoClicker", type = "I", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerI extends Check {

    private double threshold, lastSTD;
    private final List<Integer> cpsList = new ArrayList<>();
    private int movements;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 10 && !getUser().getClickProcessor().isDigging()) {

                cpsList.add(movements);

                if (cpsList.size() == 100) {

                    double std = ClickUtils.getStandardDeviation(cpsList);

                    double difference = Math.abs(std - this.lastSTD);


                    if (difference <= 0.01) {
                        if (++threshold > 2) {
                            flag("s="+std + " d="+difference);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.1);
                    }

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