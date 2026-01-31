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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PacketCreator {

    /**
     *
     * Makes an entity spawn packet for an itemdisplay at the given location with the given entityID.
     * Yaw, pitch, data, velocity & headRot will always be 0.
     *
     * @param location The location to get the x, y, z values from.
     * @param entityID The entityID, this should be gotten through the players EntityHandler
     * @return The ClientBoundAddEntityPacket.
     */
    public static ClientboundAddEntityPacket addItemDisplayPacket(Location location, int entityID) {
        return new ClientboundAddEntityPacket(
            entityID,
                UUID.randomUUID(),
                location.x(),
                location.y(),
                location.z(),
                0,
                0,
                net.minecraft.world.entity.EntityType.ITEM_DISPLAY,
                0,
                Vec3.ZERO,
                0
        );
    }

    public static ClientboundSetEntityDataPacket setItemDisplayDataPacket(int entityID) {

        EntityDataSerializer<ItemStack> serializer = EntityDataSerializers.ITEM_STACK;

        List<SynchedEntityData.DataValue<?>> data = new ArrayList<>();

        SynchedEntityData.DataValue<ItemStack> interpolationDuration =
                new SynchedEntityData.DataValue<>(
                        23,
                        serializer,
                        ItemStack.fromBukkitCopy(new org.bukkit.inventory.ItemStack(Material.DIAMOND_BLOCK)));

        data.add(interpolationDuration);
        return new ClientboundSetEntityDataPacket(
            entityID,
                data
        );
    }
}
