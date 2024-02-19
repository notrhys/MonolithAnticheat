package me.rhys.anticheat.version.impl;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.nms.impl.Instance_1_7_R4;
import me.rhys.anticheat.base.nms.impl.Instance_1_8_R1;
import me.rhys.anticheat.base.nms.impl.Instance_1_8_R2;
import me.rhys.anticheat.base.nms.impl.Instance_1_8_R3;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.version.api.IVersion;

public class VanillaHook implements IVersion {

    @Override
    public int getClientVersion(User user) {

        if (Plugin.getInstance().getNmsManager().getNmsAbstraction() instanceof Instance_1_7_R4) {
            return 5;
        } else if (Plugin.getInstance().getNmsManager().getNmsAbstraction() instanceof Instance_1_8_R1
                || Plugin.getInstance().getNmsManager().getNmsAbstraction() instanceof Instance_1_8_R2
                || Plugin.getInstance().getNmsManager().getNmsAbstraction() instanceof Instance_1_8_R3) {
            return 47;
        }

        return 0;
    }
}
