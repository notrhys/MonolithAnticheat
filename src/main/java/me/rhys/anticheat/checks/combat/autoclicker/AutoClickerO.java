package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.GraphUtil;
import me.rhys.anticheat.util.math.ClickUtils;
import me.rhys.anticheat.util.math.MathUtil;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "AutoClicker", type = "O", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerO extends Check {

    private double threshold, lastSkewness;
    private final List<Integer> cpsList = new ArrayList<>();
    private final List<Double> skewnessList = new ArrayList<>();
    private int movements;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 8 && !getUser().getClickProcessor().isDigging()) {

                this.cpsList.add(movements);

                if (cpsList.size() == 20) {

                    double cps = ClickUtils.getCPS(this.cpsList);

                    this.skewnessList.add(cps);

                    GraphUtil.GraphResult result = GraphUtil.getGraph(skewnessList);

                    if (this.skewnessList.size() > 3) {

                        int neg = result.getNegatives(), pos = result.getPositives();

                        if (neg <= 1 && pos > 35) {
                            this.threshold += 0.09;
                        } else {
                            this.threshold -= Math.min(threshold, 0.05);
                        }

                        if (neg > 6 && pos >= 25) {
                            this.threshold += 0.5;
                        } else {
                            this.threshold -= Math.min(threshold, 0.25);
                        }

                        if (threshold > 1.45) {
                            flag("t="+threshold + " n="+neg + " p="+pos);
                        }


                        if (this.skewnessList.size() >= 50) {
                            this.skewnessList.clear();
                        }
                    }


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