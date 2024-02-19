package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockPlacePacket;
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
@CheckInfo(name = "AutoClicker", type = "H", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerH extends Check {

    private double threshold;
    private int movements;
    private final List<Integer> blockDelays = new ArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.BLOCK_PLACE)) {

            boolean valid = getUser().isSword(getUser().getPlayer().getItemInHand()) ||
                    getUser().getPlayer().getItemInHand().getType().isBlock();


            if (movements < 10 && valid) {

                blockDelays.add(movements);

                if (blockDelays.size() >= 20) {

                    double cps = ClickUtils.getCPS(blockDelays);

                    if (cps > 23) {
                        if (++threshold > 3) {
                            flag("Right clicking to fast", "cps: " + cps);
                        }
                    } else {
                        threshold -= Math.min(threshold, 0.5);
                    }
                }

                if (blockDelays.size() == 100) {
                    blockDelays.clear();
                }

            }

            movements = 0;
        }

        if (event.isMovement()) {
            movements++;
        }
    }
}