package me.rhys.anticheat.base.event.api;

import me.rhys.anticheat.base.event.impl.Event;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.User;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"ForLoopReplaceableByForEach", "MismatchedQueryAndUpdateOfCollection"})
public class EventManager {
    private final List<Event> events = new ArrayList<>();

    public void callEvent(User user, PacketEvent event) {
        int size = this.events.size();

        for (int i = 0; i < size; i++) {
            this.events.get(i).onPacket(event);
        }
    }
}
