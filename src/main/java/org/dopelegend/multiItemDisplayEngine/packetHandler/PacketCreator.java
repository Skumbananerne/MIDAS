package org.dopelegend.multiItemDisplayEngine.packetHandler;

import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.ItemDisplayPacketData;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.util.List;
import java.util.UUID;

public class PacketCreator {

    /**
     *
     * Makes an entity spawn packet for an itemdisplay at the given location with the given entityID.
     * Yaw, pitch, data, velocity & headRot will always be 0.
     *
     * @param coordinates The Triple to get the x, y, z values from.
     * @param entityID The entityID, this should be gotten through the players EntityHandler
     * @return The ClientBoundAddEntityPacket.
     */
    public static ClientboundAddEntityPacket addItemDisplayPacket(Triple coordinates, int entityID) {
        return new ClientboundAddEntityPacket(
            entityID,
                UUID.randomUUID(),
                coordinates.x,
                coordinates.y,
                coordinates.z,
                0,
                0,
                net.minecraft.world.entity.EntityType.ITEM_DISPLAY,
                0,
                Vec3.ZERO,
                0
        );
    }

    /**
     *
     * Creates a ClientBoundSetEntityDataPacket, specifically for itemDisplays.
     *
     * @param packetData An ItemDisplayPacketData with any data you want send.
     * @param entityID The id of the entity to change the data for.
     * @return The ClientBoundSetEntityDataPacket
     */
    public static ClientboundSetEntityDataPacket setItemDisplayDataPacket(ItemDisplayPacketData packetData, int entityID) {
        List<SynchedEntityData.DataValue<?>> data = packetData.getPacketData();

        return new ClientboundSetEntityDataPacket(
            entityID,
                data
        );
    }

    public static ClientboundBundleDelimiterPacket bundleDelimiterPacket() {
        return new ClientboundBundleDelimiterPacket();
    }

    /**
     *
     * Creates a RemoveEntitiesPacket for a intList of Entity IDs.
     *
     * @param entityIDs The entityIDs to create the packet with.
     * @return The packet
     */
    public static ClientboundRemoveEntitiesPacket removeItemDisplaysPacket(int... entityIDs) {
        return new ClientboundRemoveEntitiesPacket(entityIDs);
    }


    public static ClientboundTeleportEntityPacket teleportEntityPacket(int entityID, Triple relCoords){
        PositionMoveRotation pos = new PositionMoveRotation(relCoords.toVec3(), Vec3.ZERO, 0, 0);



        return new ClientboundTeleportEntityPacket(
            entityID,
            pos,
            Relative.ALL,
                false
        );
    }
}
