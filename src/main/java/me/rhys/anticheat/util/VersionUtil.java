package me.rhys.anticheat.util;

import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;

public class VersionUtil {

    public static Versions getClientVersion(User user) {

        switch (Plugin.getInstance().getVersionHook().getVersion().getClientVersion(user)) {
            case 5: return Versions.V1_7;

            case 47: return Versions.V1_8;

            case 107:
            case 109:
            case 110:
                return Versions.V1_9;

            case 210:
                return Versions.V_10;

            case 315:
            case 316:
                return Versions.V_11;

            case 335:
            case 338:
            case 340:
                return Versions.V_12;

            case 393:
            case 401:
            case 404:
                return Versions.V_13;

            case 477:
            case 480:
            case 485:
            case 490:
            case 498:
                return Versions.V_14;

            case 573:
            case 575:
            case 578:
                return Versions.V_15;

            case 721:
            case 722:
            case 725:
            case 727:
            case 729:
            case 730:
            case 732:
            case 733:
            case 734:
            case 735:
            case 736:
            case 751:
            case 753:
            case 754:
                return Versions.V_16;

            case 755:
            case 756:
                return Versions.V_17;

            case 757: return Versions.V1_18;
        }

        return Versions.UNKNOWN;
    }

    public enum Versions {
        UNKNOWN,
        V1_7,
        V1_8,
        V1_9,
        V_10,
        V_11,
        V_12,
        V_13,
        V_14,
        V_15,
        V_16,
        V_17,
        V1_18
    }
}
