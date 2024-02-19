package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.GraphUtil;
import me.rhys.anticheat.util.math.ClickUtils;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "AutoClicker", type = "M", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerM extends Check {

    private double threshold, lastSkewness;
    private final List<Integer> cpsList = new ArrayList<>();
    private int movements;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 8 && !getUser().getClickProcessor().isDigging()) {

                this.cpsList.add(movements);

                if (cpsList.size() == 100) {


                    double skewness = ClickUtils.getSkewness(this.cpsList);

                    double difference = Math.abs(skewness - this.lastSkewness);

                    if (difference < 0.1) {
                        if (++threshold > 3) {
                            flag("d="+difference + " s="+skewness + " ls="+lastSkewness);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.08);
                    }

                    this.lastSkewness = skewness;
                    this.cpsList.clear();
                }
            }

            this.movements = 0;
        }

        if (event.isMovement()) {
            this.movements++;
        }
    }
}