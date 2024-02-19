package me.rhys.anticheat.config;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter @Setter
public class ConfigValues {
    private String alertMessage;
    private boolean hoverDebug;

    private boolean hider;
    private boolean blockTabComplete;
    private boolean blockGeneralCommands;
    private String hiderName;
    private boolean faggotKick;

    private boolean punish;
    private int maxViolations;
    private List<String> punishCommands = new ArrayList<>();
    private List<String> punishMessages = new ArrayList<>();
}
