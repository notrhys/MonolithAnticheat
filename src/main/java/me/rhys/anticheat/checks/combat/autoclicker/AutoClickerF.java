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
@CheckInfo(name = "AutoClicker", type = "F", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerF extends Check {

    private double threshold;
    private int movements;
    private final List<Integer> cpsList = new ArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {

            if (movements < 8 && !getUser().getClickProcessor().isDigging()) {

                cpsList.add(movements);

                if (cpsList.size() == 1000) {

                    int outliers = (int) cpsList.stream().filter(delay -> delay > 3).count();

                    if (outliers < 7) {
                        if (++threshold > 2) {
                            flag("o="+outliers);
                        }
                    } else {
                        threshold -= Math.min(threshold, 1.2);
                    }

                    cpsList.clear();
                }
            }
        }

        if (event.isMovement()) {
            movements++;
        }
    }
}