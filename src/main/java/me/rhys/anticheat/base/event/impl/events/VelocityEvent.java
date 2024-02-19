package me.rhys.anticheat.base.event.impl.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class VelocityEvent {
    private final double x;
    private final double y;
    private final double z;
    private final double deltaY;
    private final int tick;
}
