package me.rhys.anticheat.version.impl;

import me.rhys.anticheat.base.user.User;
import me.rhys.anticheat.version.api.IVersion;
import protocolsupport.api.ProtocolSupportAPI;

public class ProtocolSupport implements IVersion {

    @Override
    public int getClientVersion(User user) {
        return ProtocolSupportAPI.getProtocolVersion(user.getPlayer()).getId();
    }
}
