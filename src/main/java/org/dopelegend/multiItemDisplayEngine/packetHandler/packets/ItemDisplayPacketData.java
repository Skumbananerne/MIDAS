package org.dopelegend.multiItemDisplayEngine.packetHandler.packets;

import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.item.ItemStack;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.List;

public class ItemDisplayPacketData extends DisplayPacketData implements PacketData{
    ItemStack displayedItem = null;
    Byte displayedType = Byte.MIN_VALUE;

    public Byte getDisplayedType() {
        return displayedType;
    }

    public void setDisplayedType(Byte displayedType) {
        this.displayedType = displayedType;
    }

    public ItemStack getDisplayedItem() {
        return displayedItem;
    }

    public void setDisplayedItem(org.bukkit.inventory.ItemStack displayedItem) {
        this.displayedItem = ItemStack.fromBukkitCopy(displayedItem);
    }

    @Override
    public List<SynchedEntityData.DataValue<?>> getPacketData(){
        List<SynchedEntityData.DataValue<?>> data = new ArrayList<>(super.getPacketData());

        if(displayedItem != null){
            EntityDataSerializer<ItemStack> serializer = EntityDataSerializers.ITEM_STACK;

            SynchedEntityData.DataValue<ItemStack> item =
                    new SynchedEntityData.DataValue<>(
                            23,
                            serializer,
                            displayedItem);

            data.add(item);
        }
        if(displayedType != null){
            EntityDataSerializer<Byte> serializer = EntityDataSerializers.BYTE;

            SynchedEntityData.DataValue<Byte> item =
                    new SynchedEntityData.DataValue<>(
                            24,
                            serializer,
                            displayedType);

            data.add(item);
        }

        return data;
    }
}
