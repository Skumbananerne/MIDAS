package org.dopelegend.multiItemDisplayEngine.rotation;

import org.bukkit.entity.ItemDisplay;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Rotate {

    /**
     *
     * Sets the rotation of an ItemDisplayGroup around all 3 axis using a 4x4 matrix.
     *
     * @param itemDisplayGroup The itemDisplayGroup to rotate
     * @param rotation The rotation which is an euler angle using degrees (360)
     */
    public static void SetRotationItemDisplayGroup(ItemDisplayGroup itemDisplayGroup, Triple rotation) {
        Bone rootBone = itemDisplayGroup.getRootBone();
        for (Bone bone : rootBone.getAllChildrenBones(true)){
            if (!bone.hasElement()) continue;
            ItemDisplay itemDisplay = bone.getItemDisplay();
            Matrix4f currentMatrix = new Matrix4f();
            currentMatrix.rotateXYZ(new Vector3f((float) Math.toRadians(rotation.x), (float) Math.toRadians(rotation.y), (float) Math.toRadians(rotation.z)));
            itemDisplay.setTransformationMatrix(currentMatrix);
        }
    }

    /**
     *
     * Rotates an ItemDisplayGroup around all 3 axis using the itemDisplay's transformation.
     *
     * @param itemDisplayGroup The itemDisplayGroup to rotate
     * @param rotation The rotation to add which is an euler angle (3 angles) using degrees (360)
     */
    public static void AddRotationItemDisplayGroup(ItemDisplayGroup itemDisplayGroup, Triple rotation) {
        Triple oldRotation = itemDisplayGroup.getRotationInEulerAngles();
        Triple newRotation = new Triple(oldRotation.x + rotation.x, oldRotation.y + rotation.y, oldRotation.z + rotation.z);
        SetRotationItemDisplayGroup(itemDisplayGroup, newRotation);
    }
}
