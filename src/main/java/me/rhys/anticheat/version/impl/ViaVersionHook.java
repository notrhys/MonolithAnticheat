package me.rhys.anticheat.version.impl;

import com.viaversion.viaversion.api.Via;
import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.version.api.IVersion;

public class ViaVersionHook implements IVersion {

    @Override
    public int getClientVersion(User user) {
        return Via.getAPI().getPlayerVersion(user.getUuid());
    }
}
