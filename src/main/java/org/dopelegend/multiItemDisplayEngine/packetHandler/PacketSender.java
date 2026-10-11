package org.dopelegend.multiItemDisplayEngine.packetHandler;

import net.minecraft.network.protocol.BundleDelimiterPacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBundleDelimiterPacket;
import net.minecraft.network.protocol.game.ClientboundBundlePacket;
import net.minecraft.server.packs.repository.Pack;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.*;

import java.util.*;

public class PacketSender {
    static Map<UUID, List<Packet<?>>> packetQueue = new HashMap<>();
    static int printNext = 0;

    private static void sendPacket(Player player, Packet<?> packet) {
        CraftPlayer craftPlayer = (CraftPlayer) player;
        craftPlayer.getHandle().connection.send(packet);
    }

    private static void sendPackets(Player player, List<Packet<?>> packets) {
        for (Packet<?> packet : packets) {
            sendPacket(player, packet);
        }
    }

    public static void queuePacket(PacketData packetData, Player player){
        if (!packetQueue.containsKey(player.getUniqueId())){
            packetQueue.put(player.getUniqueId(), new ArrayList<>());
        }

        Packet<?> packet = packetData.createPacket();
        packetQueue.get(player.getUniqueId()).add(packet);
    }

    public static void queuePacket(PacketData packetData, List<Player> players){
        Packet<?> packet = packetData.createPacket();
//        boolean thisTime = false;
//        if (packetData instanceof ItemDisplayDataPacketData displayData) {
//                Bukkit.broadcastMessage("Transformation matrix:");
//               Bukkit.broadcastMessage(displayData.getRotMatrix().toString());
//                if (displayData.getDisplayedItem() != null) {
//                    Bukkit.broadcastMessage("Displayed item: " + displayData.getDisplayedItem().toString());
//                }
//                else {
//                    Bukkit.broadcastMessage("Displayed item: null");
//                }
//
//                Bukkit.broadcastMessage("Displayed type: " + displayData.getDisplayedType().toString());
//                Bukkit.broadcastMessage("View Range: " + String.valueOf(displayData.getViewRange()));
//                Bukkit.broadcastMessage("Teleport duration: " + String.valueOf(displayData.getTeleportInterpolationDuration()));
//                Bukkit.broadcastMessage("Transformation duration: " + String.valueOf(displayData.getTransformationInterpolationDuration()));
//                Bukkit.broadcastMessage("Interpolation delay: " + String.valueOf(displayData.getInterpolationDelay()));
//
//
//            printNext = 5;
//                thisTime = true;
//        }
//        if (packetData instanceof TeleportEntityPacketData teleportData) {
//            Bukkit.broadcastMessage("Teleport: "+teleportData.getRelCoords().toString());
//        }
//        if (packetData instanceof SpawnItemDisplayPacketData spawnData) {
//            Bukkit.broadcastMessage("Spawned itemDisplay at: "+spawnData.getPosition().toString());
//            Bukkit.broadcastMessage("And EID: "+spawnData.getEntityID());
//        }
//        if (packetData instanceof RemoveEntitiesPacketData removeData) {
//            Bukkit.broadcastMessage("Removed itemDisplay(s) with EID: "+removeData.getEntityIds().toString());
//        }
//        Bukkit.broadcastMessage(players.toString());

        for(Player player : players) {
            if (!packetQueue.containsKey(player.getUniqueId())){
                packetQueue.put(player.getUniqueId(), new ArrayList<>());
            }
            packetQueue.get(player.getUniqueId()).add(packet);
        }
    }

    public static void flushPackets(){
        Map<UUID, ClientboundBundlePacket> packets = new HashMap<>();
        for (Map.Entry<UUID, List<Packet<?>>> entry : packetQueue.entrySet()) {
            ClientboundBundlePacket bundle = new ClientboundBundlePacket(
                    (Iterable) entry.getValue()
            );

            packets.put(entry.getKey(), bundle);
        }

        for (Map.Entry<UUID, ClientboundBundlePacket> entry : packets.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if(player == null) continue;
            if(!player.isOnline()) continue;

            sendPacket(player, entry.getValue());
        }

        packetQueue.clear();
    }
}
