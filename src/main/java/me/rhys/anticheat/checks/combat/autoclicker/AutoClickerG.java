package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.math.ClickUtils;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "AutoClicker", type = "G", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerG extends Check {

    private double threshold;
    private final List<Integer> cpsList = new ArrayList<>();
    private int movements;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 10 && !getUser().getClickProcessor().isDigging()) {

                cpsList.add(movements);

                if (cpsList.size() == 200) {

                    double std = ClickUtils.getStandardDeviation(cpsList);

                    if (std <= 0.47) {
                        if (++threshold > 2) {
                            flag("s="+std);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.5);
                    }

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