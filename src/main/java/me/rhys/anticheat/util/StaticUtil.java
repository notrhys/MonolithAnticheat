package me.rhys.anticheat.util;

import me.rhys.anticheat.Plugin;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class StaticUtil {

    public static final class User {
        public static final List<String> STAFF_UUID = Arrays.asList(
                "b2549f02-bbeb-461d-bc19-3ab4cc142984",
                "557748d7-217e-4717-8a50-271355e6ad18",
                "e075e4db-597d-4215-8820-cd115c697146",
                "2ca2f2bb-2d9d-4a78-9556-0b2ae5862a62",
                "1f334efc-ab5a-453b-b2d2-b11d1bf273be"
        );
    }

    public static String getAnticheatName() {
        return (Plugin.getInstance().getConfigValues().isHider() ?
                Plugin.getInstance().getConfigValues().getHiderName().toUpperCase(Locale.ROOT).charAt(0)
                        + Plugin.getInstance().getConfigValues().getHiderName().substring(1) : "Monolith");
    }
}
