package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
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
@CheckInfo(name = "AutoClicker", type = "B", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerB extends Check {

    private double threshold;
    private int movements;
    private final List<Integer> cpsList = new ArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {

            if (movements < 8 && !getUser().getClickProcessor().isDigging()) {

                cpsList.add(movements);

                if (cpsList.size() == 40) {

                    double std = ClickUtils.getStandardDeviation(cpsList);

                    if (std <= 0.3001) {
                        if (++threshold > 2) {
                            flag("std=" + std);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.125);
                    }

                    cpsList.clear();
                }
            }

            movements = 0;
        }

        if (event.isMovement()) {
            if (movements < 60) {
                movements++;
            }
        }
    }
}