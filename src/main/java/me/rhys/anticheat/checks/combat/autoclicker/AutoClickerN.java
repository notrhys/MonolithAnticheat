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
@CheckInfo(name = "AutoClicker", type = "N", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerN extends Check {

    private double threshold, lastKurtosis;
    private final List<Integer> cpsList = new ArrayList<>();
    private int movements;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 8 && !getUser().getClickProcessor().isDigging()) {

                this.cpsList.add(movements);

                if (cpsList.size() == 75) {


                    double kurtosis = ClickUtils.getKurtosis(this.cpsList);

                    double difference = Math.abs(kurtosis - this.lastKurtosis);

                    if (kurtosis < 10 && kurtosis > 0.0 && difference <= 0.03) {
                        if (++threshold > 2) {
                            flag("k="+kurtosis + " d="+difference);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.025);
                    }



                    this.lastKurtosis = kurtosis;

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