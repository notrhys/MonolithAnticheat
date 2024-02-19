package me.rhys.anticheat.base.user.processor;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import lombok.Getter;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

@Getter
public class PotionProcessor {
    private final User user;

    public PotionProcessor(User user) {
        this.user = user;
    }

    private boolean speed, jump, slowness;
    private int speedTicks, jumpTicks, slownessTicks;

    private double speedAmplifer;
    private double jumpAmplifer;
    private double slownessAmplifer;

    public void handle(String type, Object packet, long now) {
        switch (type) {
            case Packet.Client.FLYING:
            case Packet.Client.POSITION:
            case Packet.Client.POSITION_LOOK:
            case Packet.Client.LOOK: {

                boolean jump = false;
                boolean speed = false;
                boolean slowness = false;

                int speedAmplifer = 0;
                int jumpAmplifer = 0;
                int slowAmplifer = 0;

                for (PotionEffect potionEffect : Plugin.getInstance().getNmsManager().getNmsAbstraction()
                        .potionEffectList(user)) {

                    if (!user.getCheckManager().isLoadedAll()) continue;

                    PotionEffectType potionEffectType = potionEffect.getType();
                    int amplifer = potionEffect.getAmplifier() + 1;

                    if (potionEffectType.equals(PotionEffectType.SPEED)) {
                        speedAmplifer = amplifer;
                        speed = true;
                    }

                    if (potionEffectType.equals(PotionEffectType.JUMP)) {
                        jumpAmplifer = amplifer;
                        jump = true;
                    }

                    if (potionEffectType.equals(PotionEffectType.SLOW)) {
                        slowAmplifer = amplifer;
                        slowness = true;
                    }
                }

                this.slowness = slowness;
                this.jump = jump;
                this.speed = speed;

                if (slowness) {
                    this.slownessTicks += (this.slownessTicks < 20 ? 1 : 0);
                } else {
                    this.slownessTicks -= (this.slownessTicks > 0 ? 1 : 0);
                }

                if (jump) {
                    this.jumpTicks += (this.jumpTicks < 20 ? 1 : 0);
                } else {
                    this.jumpTicks -= (this.jumpTicks > 0 ? 1 : 0);
                }

                if (speed) {
                    this.speedTicks += (this.speedTicks < 20 ? 1 : 0);
                } else {
                    this.speedTicks -= (this.speedTicks > 0 ? 1 : 0);
                }

                this.speedAmplifer = speedAmplifer;
                this.jumpAmplifer = jumpAmplifer;
                this.slownessAmplifer = slowAmplifer;

                break;
            }
        }
    }
}
