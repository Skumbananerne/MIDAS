package org.dopelegend.multiItemDisplayEngine.packetHandler.packets;

import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.ArrayList;
import java.util.List;

public abstract interface PacketData {
    public List<SynchedEntityData.DataValue<?>> getPacketData();
}
