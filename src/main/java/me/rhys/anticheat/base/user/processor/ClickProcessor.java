package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInBlockDigPacket;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInUseEntityPacket;
import lombok.Getter;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.util.math.ClickUtils;
import me.rhys.anticheat.util.math.EventTimer;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ClickProcessor {
    private final User user;

    private final List<Integer> clickDelays = new ArrayList<>();
    private final EventTimer digTimer;

    private boolean digging = false, releaseUseItem;
    private int movements;
    private double cps, skewness, median, duplicates, kurtosis, std, average;

    public ClickProcessor(User user) {
        this.user = user;
        this.digTimer = new EventTimer(20, user);
    }

    public void handle(String type, Object packet, long now) {

        if (!getUser().getCheckManager().isLoadedAll()) return;

        if (type.equalsIgnoreCase(Packet.Client.BLOCK_DIG)) {
            WrappedInBlockDigPacket wrapped = new WrappedInBlockDigPacket(packet, this.user.getPlayer());

            switch (wrapped.getAction()) {
                case RELEASE_USE_ITEM: {
                    this.releaseUseItem = true;
                    break;
                }

                case START_DESTROY_BLOCK: {
                    this.digTimer.reset();
                    this.digging = true;
                    break;
                }
            }
        }

        if (type.equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {

            if (this.movements < 10 && !this.releaseUseItem && !this.digging) {
                clickDelays.add(this.movements);

                if (this.clickDelays.size() >= 20) {

                    this.cps = ClickUtils.getCPS(this.clickDelays);
                    this.median = ClickUtils.getMedian(this.clickDelays);
                    this.skewness = ClickUtils.getSkewness(this.clickDelays);
                    this.std = ClickUtils.getStandardDeviation(this.clickDelays);
                    this.duplicates = ClickUtils.getDuplicates(this.clickDelays);
                    this.kurtosis = ClickUtils.getKurtosis(this.clickDelays);
                    this.average = ClickUtils.getAverage(this.clickDelays);
                }

                if (clickDelays.size() >= 120) {
                    this.clickDelays.clear();
                }
            }

            this.movements = 0;
        }

        if (type.equalsIgnoreCase(Packet.Client.USE_ENTITY)) {
            WrappedInUseEntityPacket entityPacket = new WrappedInUseEntityPacket(packet, getUser().getPlayer());

            if (entityPacket.getAction() == WrappedInUseEntityPacket.EnumEntityUseAction.ATTACK) {
                this.digging = false;
            }
        }

        if (type.equalsIgnoreCase(Packet.Client.POSITION_LOOK)
                | type.equalsIgnoreCase(Packet.Client.LOOK)
                || type.equalsIgnoreCase(Packet.Client.POSITION)) {
            this.movements++;
        }
    }
}
