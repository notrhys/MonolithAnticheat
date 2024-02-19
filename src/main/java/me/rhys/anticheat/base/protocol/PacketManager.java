package me.rhys.anticheat.base.protocol;

import cc.funkemunky.api.Atlas;
import cc.funkemunky.api.tinyprotocol.api.Packet;
import cc.funkemunky.api.tinyprotocol.listener.functions.PacketListener;
import cc.funkemunky.api.tinyprotocol.packet.in.WrappedInTabComplete;
import me.rhys.anticheat.Plugin;
import me.rhys.anticheat.base.user.User;
import org.bukkit.event.EventPriority;

public class PacketManager {

    public final PacketListener listener = Atlas.getInstance().getPacketProcessor().process(Plugin.getServerInstance(),
            EventPriority.NORMAL, info -> {

                final User user = Plugin.getInstance().getUserManager().getUser(info.getPlayer());

                if (user == null) return;

                user.getPacketProcessor().handlePacket(info.getType(), info.getPacket(), info.getTimestamp());

                if (info.getType().equalsIgnoreCase(Packet.Client.TAB_COMPLETE) && !user.getPlayer().isOp()) {
                    final WrappedInTabComplete wrappedInTabComplete = new WrappedInTabComplete(info.getPacket(),
                            user.getPlayer());

                    final String message = wrappedInTabComplete.getMessage().toLowerCase();

                    if ((message.startsWith("/") && !message.contains(" "))
                            || message.startsWith("/?")
                            || message.startsWith("/minecraft:")
                            || message.startsWith("/help")
                            || message.startsWith("/about")) {
                        info.setCancelled(true);
                    }
                }
            });
}
