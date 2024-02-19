package me.rhys.anticheat.checks.other;

import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInCustomPayload;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInWindowClickPacket;
import me.rhys.anticheat.base.check.api.Check;
import me.rhys.anticheat.base.check.api.CheckInfo;
import me.rhys.anticheat.base.check.api.CheckType;
import me.rhys.anticheat.base.event.impl.events.PacketEvent;
import me.rhys.anticheat.base.user.User;

import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CheckInfo(name = "Payload", type = "A", checkType = CheckType.OTHER, enabled = true)
public class PayloadA extends Check {

    private final Map<String, String> clientMap = Stream.of(
                    new AbstractMap.SimpleEntry<>("SRT-Hellcat", "SRT_HellCat_Client"),
                    new AbstractMap.SimpleEntry<>("Vanilla", "Jigsaw_Client"))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

    private boolean detected;
    private String payload;

    @Override
    public void onPacket(PacketEvent event) {
        User user = getUser();

        if (event.getType().equalsIgnoreCase(Packet.Client.CUSTOM_PAYLOAD)) {
            WrappedInCustomPayload wrapped = new WrappedInCustomPayload(event.getPacket(), user.getPlayer());

            String channel = wrapped.getTag();
            String data = this.getDecodedData(wrapped);

            this.clientMap.forEach((s, s2) -> {
                if (s2.equalsIgnoreCase(channel) || s2.equalsIgnoreCase(data)) {
                    this.payload = s;
                    this.detected = true;
                }
            });
        }

        if (event.isMovement() && this.detected) {
            this.flag("client=" + this.payload);
        }
    }

    public String getDecodedData(WrappedInCustomPayload wrapped) {
        return new String(wrapped.getData(), StandardCharsets.UTF_8)
                .replaceAll("\\P{Print}", "");
    }
}
