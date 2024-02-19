package me.rhys.anticheat.version.api;

import me.rhys.anticheat.base.user.User;

public interface IVersion {
    int getClientVersion(User user);
}
