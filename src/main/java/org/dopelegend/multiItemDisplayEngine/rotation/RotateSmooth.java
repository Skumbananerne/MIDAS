package org.dopelegend.multiItemDisplayEngine.rotation;

import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

public class RotateSmooth {

    /**
     *
     * Smoothly sets the rotation of an ItemDisplayGroup using 4x4 matrices.
     *
     * @param itemDisplayGroup The ItemDisplayGroup to rotate
     * @param rotation The rotation as an euler angle in degrees
     * @param interpolationDuration The time it takes the itemDisplayGroup to get from its start rotation to the given rotation
     */
    public static void SetRotationItemDisplayGroupSmooth(ItemDisplayGroup itemDisplayGroup, Triple rotation, int interpolationDuration) {
        for (Bone bone : itemDisplayGroup.getRootBone().getAllChildrenBones(true)){
            if(!bone.hasElement()) continue;
            bone.getItemDisplay().setInterpolationDuration(interpolationDuration);
            bone.getItemDisplay().setInterpolationDelay(0);
        }

        itemDisplayGroup.setRotation(rotation);
    }

    /**
     *
     * Smoothly adds some rotation to an ItemDisplayGroup using 4x4 matrices.
     *
     * @param itemDisplayGroup The ItemDisplayGroup to rotate
     * @param rotation The rotation to add as an euler angle in degrees
     * @param interpolationDuration The time it takes the itemDisplayGroup to get from its start rotation to the end rotation
     */
    public static void AddRotationItemDisplayGroupSmooth(ItemDisplayGroup itemDisplayGroup, Triple rotation, int interpolationDuration) {
        for (Bone bone : itemDisplayGroup.getRootBone().getAllChildrenBones(true)){
            if(!bone.hasElement()) continue;
            bone.getItemDisplay().setInterpolationDuration(interpolationDuration);
            bone.getItemDisplay().setInterpolationDelay(0);
        }

        itemDisplayGroup.addRotation(rotation);
    }
}
