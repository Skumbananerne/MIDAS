package org.dopelegend.multiItemDisplayEngine.movement;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

public class Teleport {

    /**
     *
     * Teleports an entire itemDisplayGroup to some location
     *
     * @param itemDisplayGroup The itemDisplayGroup to teleport
     * @param location The location to teleport the itemDisplayGroup to.
     */
    public static void teleportItemDisplayGroup(ItemDisplayGroup itemDisplayGroup, Location location) {
        Bone rootBone = itemDisplayGroup.getRootBone();
        Location teleportLocation = new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), 0, 0);
        for (Bone bone : rootBone.getAllChildrenBones(true)) {
            if (!bone.hasElement()) continue;
            ItemDisplay itemDisplay = bone.getItemDisplay();
            Triple boneOffset = bone.getOffset();
            itemDisplay.teleport(teleportLocation.clone().add(boneOffset.x, boneOffset.y, boneOffset.z));
        }
        itemDisplayGroup.setPivotPoint(teleportLocation);
    }


    public static void teleportItemDisplayGroupRelative(ItemDisplayGroup itemDisplayGroup, Triple relativeCoordinates) {
        Bone rootBone = itemDisplayGroup.getRootBone();
        Location pivotPoint = itemDisplayGroup.getPivotPoint();
        Location teleportLocation = new Location(pivotPoint.getWorld(), pivotPoint.getX()+relativeCoordinates.x, pivotPoint.getY()+relativeCoordinates.y, pivotPoint.getZ()+relativeCoordinates.z, 0, 0);
        for (Bone bone : rootBone.getAllChildrenBones(true)) {
            if (!bone.hasElement()) continue;
            ItemDisplay itemDisplay = bone.getItemDisplay();
            Triple boneOffset = bone.getOffset();
            itemDisplay.teleport(teleportLocation.clone().add(boneOffset.x, boneOffset.y, boneOffset.z));
        }
        itemDisplayGroup.setPivotPoint(teleportLocation);
    }

}
