package org.dopelegend.multiItemDisplayEngine.packetHandler.packets;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.phys.Vec3;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.util.UUID;

public class SpawnItemDisplayPacketData implements PacketData {
    private Triple position;
    private int entityID;

    public Triple getPosition() {
        return position;
    }

    public void setPosition(Triple position) {
        this.position = position;
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    /**
     *
     * Makes an entity spawn packet for an itemdisplay at the given location with the given entityID.
     * Yaw, pitch, data, velocity & headRot will always be 0.
     *
     * @return The ClientBoundAddEntityPacket.
     */
    @Override
    public ClientboundAddEntityPacket createPacket(){
        return new ClientboundAddEntityPacket(
                entityID,
                UUID.randomUUID(),
                position.x,
                position.y,
                position.z,
                0,
                0,
                net.minecraft.world.entity.EntityType.ITEM_DISPLAY,
                0,
                Vec3.ZERO,
                0
        );
    }

    @Override
    public SpawnItemDisplayPacketData clone(){
        SpawnItemDisplayPacketData clone = new SpawnItemDisplayPacketData();
        clone.setEntityID(entityID);
        clone.setPosition(position.clone());
        return clone;
    }
}
