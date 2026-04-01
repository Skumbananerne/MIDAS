package org.dopelegend.multiItemDisplayEngine.packetHandler;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class PacketSender {
    public static void sendPacket(Player player, Packet<?> packet) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().connection.send(packet);
    }

    public static void sendPackets(Player player, List<Packet<?>> packets) {
        for (Packet<?> packet : packets) {
            sendPacket(player, packet);
        }
    }
}
