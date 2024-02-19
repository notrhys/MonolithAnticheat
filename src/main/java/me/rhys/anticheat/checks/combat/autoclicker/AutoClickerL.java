package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.GraphUtil;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "AutoClicker", type = "L", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerL extends Check {

    private double threshold;
    private final List<Double> cpsList = new ArrayList<>();
    private int movements;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 8 && !getUser().getClickProcessor().isDigging()) {

                if (getUser().getClickProcessor().getCps() > 9) {
                    cpsList.add((double) movements);
                }

                if (cpsList.size() == 40) {

                    GraphUtil.GraphResult result = GraphUtil.getGraph(cpsList);

                    if (result.getPositives() == 0) {
                        if (++threshold > 8) {
                            flag("grp="+result.getPositives() + " grn="+result.getNegatives());
                        }
                    } else {
                        threshold = 0;
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