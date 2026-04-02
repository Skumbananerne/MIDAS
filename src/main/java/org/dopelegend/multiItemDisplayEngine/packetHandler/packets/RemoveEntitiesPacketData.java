package org.dopelegend.multiItemDisplayEngine.packetHandler.packets;

import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;

import java.util.Set;

public class RemoveEntitiesPacketData implements PacketData {
    Set<Integer> entityIds;

    public Set<Integer> getEntityIds() {
        return entityIds;
    }

    public void setEntityIds(Set<Integer> entityIds) {
        this.entityIds = entityIds;
    }

    public void addEntityID(int entityID){
        this.entityIds.add(entityID);
    }

    public void removeEntityID(int entityID){
        entityIds.remove(entityID);
    }

    /**
     *
     * Creates a RemoveEntitiesPacket with the given entityIDs.
     *
     * @return The RemoveEntitiesPacket
     */
    @Override
    public ClientboundRemoveEntitiesPacket createPacket(){
        return new ClientboundRemoveEntitiesPacket(entityIds.stream().mapToInt(Integer::intValue).toArray());
    }
}
