package org.dopelegend.multiItemDisplayEngine.movement;

import org.bukkit.Location;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

public class TeleportSmooth {

    /**
     *
     * Teleports an itemDisplayGroup to a location smoothly (with interpolation). This doesn't affect the rotation.
     *
     * @param itemDisplayGroup The itemDisplayGroup to teleport.
     * @param location The location to teleport the itemDisplayGroup to.
     * @param teleportDuration How long the teleport should take. (How long it should take for the itemDisplayGroup to get from start loc to end loc)
     */
    public static void TeleportItemDisplayGroupSmooth(ItemDisplayGroup itemDisplayGroup, Location location, int teleportDuration){

        for (Bone bone : itemDisplayGroup.getRootBone().getAllChildrenBones(true)){
            if(!bone.hasElement()) continue;
            bone.getItemDisplay().setTeleportDuration(teleportDuration);
        }

        itemDisplayGroup.teleport(location);
    }

    /**
     *
     * Teleports an itemDisplayGroup to a relative location smoothly (with interpolation). This doesn't affect the rotation.
     *
     * @param itemDisplayGroup The itemDisplayGroup to teleport.
     * @param relativeCoordinates The relative coordinates to teleport the itemDisplayGroup to.
     * @param teleportDuration How long the teleport should take. (How long it should take for the itemDisplayGroup to get from start loc to end loc)
     */
    public static void TeleportItemDisplayGroupRelativeSmooth(ItemDisplayGroup itemDisplayGroup, Triple relativeCoordinates, int teleportDuration){

        for (Bone bone : itemDisplayGroup.getRootBone().getAllChildrenBones(true)){
            if(!bone.hasElement()) continue;
            bone.getItemDisplay().setTeleportDuration(teleportDuration);
        }

        itemDisplayGroup.teleportRelative(relativeCoordinates);
    }
}
