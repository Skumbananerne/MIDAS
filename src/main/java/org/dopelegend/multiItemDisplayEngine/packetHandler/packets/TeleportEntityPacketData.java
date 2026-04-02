package org.dopelegend.multiItemDisplayEngine.packetHandler.packets;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.world.entity.PositionMoveRotation;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.phys.Vec3;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

public class TeleportEntityPacketData implements PacketData {
    private int entityID;
    private Triple relCoords;

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    public Triple getRelCoords() {
        return relCoords;
    }

    public void setRelCoords(Triple relCoords) {
        this.relCoords = relCoords;
    }

    @Override
    public ClientboundTeleportEntityPacket createPacket() {
        PositionMoveRotation pos = new PositionMoveRotation(relCoords.toVec3(), Vec3.ZERO, 0, 0);

        return new ClientboundTeleportEntityPacket(
                entityID,
                pos,
                Relative.ALL,
                false
        );
    }
}
