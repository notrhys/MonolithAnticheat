package me.rhys.anticheat.checks.other.badpackets;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.math.ClickUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Experimental
@CheckInfo(name = "BadPackets", type = "I", checkType = CheckType.OTHER, enabled = true)
public class BadPacketsI extends Check {

    private int stage;
    private double threshold, lastSTD;
    private final List<Integer> delays = new ArrayList<>();

    @Override
    public void onPacket(PacketEvent event) {
        switch (event.getType()) {

            case Packet.Client.FLYING:
            case Packet.Client.LOOK:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.POSITION: {

                if (stage != -1 && getUser().getCombatProcessor().getLastAttacked() instanceof Player) {

                    if (getUser().getCombatProcessor().getAttackTimer().hasNotPassed(1)
                            && getUser().getMovementProcessor().getDeltaXZ() > 0.09f) {
                        this.delays.add(this.stage);

                        if (this.delays.size() == 40) {

                            double std = ClickUtils.getStandardDeviation(this.delays);

                            double difference = Math.abs(std - this.lastSTD);

                            if (std < 0.9 && std > 0.799 && difference < 0.007) {
                                if (++threshold > 3) {
                                    flag("s="+std + " d="+difference);
                                }
                            } else {
                                threshold -= Math.min(threshold, 0.17);
                            }

                            this.lastSTD = std;
                            this.delays.clear();
                        }
                    }
                }

                this.stage = -1;


                break;
            }

            case Packet.Client.USE_ENTITY: {
                WrappedInUseEntityPacket useEntityPacket =
                        new WrappedInUseEntityPacket(event.getPacket(), getUser().getPlayer());

                if (useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.INTERACT
                        || useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.INTERACT_AT) {
                    if (useEntityPacket.getEntity() instanceof Player) {
                        this.stage = 0;
                    }
                }

                if (useEntityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
                    this.stage = 1;
                }

                break;
            }
            case Packet.Client.BLOCK_PLACE: {
                this.stage = 2;
                break;
            }

            case Packet.Client.BLOCK_DIG: {
                WrappedInBlockDigPacket digPacket =
                        new WrappedInBlockDigPacket(event.getPacket(), getUser().getPlayer());

                if (digPacket.getAction() == WrappedInBlockDigPacket.EnumPlayerDigType.RELEASE_USE_ITEM) {
                    this.stage = 3;
                }
                break;
            }
        }
    }
}