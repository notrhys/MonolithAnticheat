package me.rhys.anticheat.checks.combat.autoclicker;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.check.api.Experimental;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.util.Tuple;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

@Experimental
@CheckInfo(name = "AutoClicker", type = "K", checkType = CheckType.COMBAT, enabled = true)
public class AutoClickerK extends Check {

    private double threshold, lastSTD;
    private Map<UUID, Tuple<Long, List<Long>>> lastClick;
    private Map<UUID, LinkedBlockingDeque<Double>> average = new HashMap<>();
    private int movements;


    @SuppressWarnings("unchecked")
    public AutoClickerK() {
        Executors.newFixedThreadPool(8).execute(() -> this.lastClick
                = (Map<UUID, Tuple<Long, List<Long>>>)new me.rhys.anticheat.util.Map());
    }

    @Override
    public void onPacket(PacketEvent event) {

        if (event.getType().equalsIgnoreCase(Packet.Client.ARM_ANIMATION)) {
            if (movements < 10 && !getUser().getClickProcessor().isDigging()) {
                long now = System.currentTimeMillis();

                Tuple<Long, List<Long>> tupleList = this.lastClick.getOrDefault(getUser().getUuid(),
                        new Tuple<>(0L, new ArrayList<>()));

                long delta = now - tupleList.getOne();

                tupleList.setOne(now);
                tupleList.getTwo().add(delta);

                if (tupleList.getTwo().size() >= 20) {
                    LongSummaryStatistics summaryStatistics
                            = tupleList.getTwo().stream().mapToLong(v -> v).summaryStatistics();

                    LinkedBlockingDeque<Double> averageList = this.average
                            .getOrDefault(getUser().getUuid(), new LinkedBlockingDeque<>());

                    if (averageList.size() > 0) {

                        double subAverage = Math.abs(summaryStatistics.getAverage() - ((averageList.getLast() < 0)
                                ? (averageList.getLast() * -1.0) : averageList.getLast()));

                        if (subAverage < 1 && subAverage > 0.0) {
                            if (++threshold > 9.0) {
                                flag("subAvg="+subAverage);
                            }
                        } else {
                            threshold -= Math.min(threshold, 0.75);

                            if (subAverage == 0.0) {
                                threshold += 1.6;
                            }
                        }
                    }

                    tupleList.getTwo().clear();
                    averageList.add(summaryStatistics.getAverage());
                    this.average.put(getUser().getUuid(), averageList);
                }

                this.lastClick.put(getUser().getUuid(), tupleList);
            }

            movements = 0;
        }

        if (event.isMovement()) {
            movements++;
        }
    }
}