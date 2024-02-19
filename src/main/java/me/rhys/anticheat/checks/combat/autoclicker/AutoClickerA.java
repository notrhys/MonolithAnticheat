package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import org.bukkit.Bukkit;

@Experimental
@CheckInfo(name = "AutoClicker", type = "A", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerA extends Check {

    private double threshold;
    private int movements, swings;
    private long lastFlag;

    @Override
    public void onPacket(PacketEvent event) {
        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (!getUser().getClickProcessor().isDigging() && !getUser().getClickProcessor().isReleaseUseItem()) {
                ++this.swings;
            }
        }

        if (event.isMovement()) {

            if (++this.movements == 20) {
                if (this.swings > 22) {
                    if (++threshold > 3) {
                        flag("Cps: " + this.swings);

                        this.lastFlag = System.currentTimeMillis();
                    }
                } else {
                    threshold -= Math.min(threshold, 0.05);

                    if ((System.currentTimeMillis() - this.lastFlag) > 2000L) {
                        threshold -= Math.min(threshold, 0.25);
                    }
                }

                this.swings = this.movements = 0;
            }
        }
    }
}