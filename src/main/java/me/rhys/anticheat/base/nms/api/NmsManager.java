package me.rhys.anticheat.base.nms.api;

import cc.funkemunky.api.tinyprotocol.api.ProtocolVersion;
import lombok.Getter;
import me.rhys.anticheat.base.nms.NmsAbstraction;
import me.rhys.anticheat.base.nms.impl.Instance_1_7_R4;
import me.rhys.anticheat.base.nms.impl.Instance_1_8_R1;
import me.rhys.anticheat.base.nms.impl.Instance_1_8_R2;
import me.rhys.anticheat.base.nms.impl.Instance_1_8_R3;

@Getter
public class NmsManager {
    private NmsAbstraction nmsAbstraction;

    public NmsManager() {
        String version = ProtocolVersion.getGameVersion().getServerVersion().replaceAll("v", "");

        switch (version) {
            case "1_8_R3": {
                this.nmsAbstraction = new Instance_1_8_R3();
                break;
            }

            case "1_8_R2": {
                this.nmsAbstraction = new Instance_1_8_R2();
                break;
            }

            case "1_8_R1": {
                this.nmsAbstraction = new Instance_1_8_R1();
                break;
            }

            case "1_7_R4": {
                this.nmsAbstraction = new Instance_1_7_R4();
                break;
            }
        }
    }
}
