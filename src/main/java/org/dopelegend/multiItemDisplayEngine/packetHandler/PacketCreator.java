package org.dopelegend.multiItemDisplayEngine.packetHandler;

import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.Material;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.ItemDisplayPacketData;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.PacketData;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;

import java.util.ArrayList;
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

    public static ClientboundSetEntityDataPacket setItemDisplayDataPacket(ItemDisplayPacketData packetData, int entityID) {


        List<SynchedEntityData.DataValue<?>> data = new ArrayList<>(packetData.getPacketData());

        return new ClientboundSetEntityDataPacket(
            entityID,
                data
        );
    }
}
