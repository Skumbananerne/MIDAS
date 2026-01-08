package org.dopelegend.multiItemDisplayEngine.rotation;

import org.bukkit.Location;
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
        rotation.x = rotation.x % 360;
        rotation.y = rotation.y % 360;
        rotation.z = rotation.z % 360;

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


    /**
     *
     * Sets the rotation of a single bone around some external pivot point.
     *
     * @param bone The bone to rotate
     * @param pivotPoint The pivotPoint to rotate around (use SetBoneRotationAroundRelative to use a relative Triple)
     * @param rotation The rotation as a triple representing euler angles in degrees.
     */
    public static void SetBoneRotationAround(Bone bone, Location pivotPoint, Triple rotation) {
        if (!bone.hasElement()) return;

        rotation.x = rotation.x % 360;
        rotation.y = rotation.y % 360;
        rotation.z = rotation.z % 360;

        ItemDisplay itemDisplay = bone.getItemDisplay();

        Matrix4f currentMatrix = new Matrix4f();
        Location currentLocation = itemDisplay.getLocation();
        Vector3f translation = new Vector3f((float) (pivotPoint.x()-currentLocation.x()), (float) (pivotPoint.y()-currentLocation.y()), (float) (pivotPoint.z()-currentLocation.z()));
        currentMatrix.translate(translation);
        currentMatrix.rotateXYZ(new Vector3f((float) Math.toRadians(rotation.x), (float) Math.toRadians(rotation.y), (float) Math.toRadians(rotation.z)));
        currentMatrix.translate(new Vector3f(-translation.x(), -translation.y(), -translation.z()));
        itemDisplay.setTransformationMatrix(currentMatrix);
    }

    /**
     *
     * Sets the rotation of a single bone.
     *
     * @param bone The bone to rotate
     * @param rotation The rotation as a triple representing euler angles in degrees.
     */
    public static void SetSingleBoneRotation(Bone bone, Triple rotation) {
        if (!bone.hasElement()) return;

        rotation.x = rotation.x % 360;
        rotation.y = rotation.y % 360;
        rotation.z = rotation.z % 360;

        ItemDisplay itemDisplay = bone.getItemDisplay();

        Matrix4f currentMatrix = new Matrix4f();
        currentMatrix.rotateXYZ(new Vector3f((float) Math.toRadians(rotation.x), (float) Math.toRadians(rotation.y), (float) Math.toRadians(rotation.z)));
        itemDisplay.setTransformationMatrix(currentMatrix);
    }

    /**
     *
     * Sets the rotation of a single bone around some relative point.
     *
     * @param bone The bone to rotate
     * @param relPoint The relative point to rotate around
     * @param rotation The rotation as a triple representing euler angles in degrees.
     */
    public static void SetBoneRotationAroundRelative(Bone bone, Triple relPoint, Triple rotation) {
        if (!bone.hasElement()) return;

        rotation.x = rotation.x % 360;
        rotation.y = rotation.y % 360;
        rotation.z = rotation.z % 360;

        ItemDisplay itemDisplay = bone.getItemDisplay();

        Matrix4f currentMatrix = new Matrix4f();
        Vector3f translation = new Vector3f((float) relPoint.x, (float) relPoint.y, (float) relPoint.z);
        currentMatrix.translate(translation);
        currentMatrix.rotateXYZ(new Vector3f((float) Math.toRadians(rotation.x), (float) Math.toRadians(rotation.y), (float) Math.toRadians(rotation.z)));
        currentMatrix.translate(new Vector3f(-translation.x(), -translation.y(), -translation.z()));
        itemDisplay.setTransformationMatrix(currentMatrix);
    }



    /**
     *
     * Adds some rotation to a single bone around some external pivot point.
     *
     * @param bone The bone to rotate
     * @param pivotPoint The pivotPoint to rotate around (use AddBoneRotationAroundRelative to use a relative Triple)
     * @param rotation The rotation as a triple representing euler angles in degrees.
     */
    public static void AddBoneRotationAround(Bone bone, Location pivotPoint, Triple rotation) {
        if (!bone.hasElement()) return;
        Triple baseRotation = bone.getBaseRotation();
        Triple oldRotation = bone.getCurrentRotation();
        Triple newRotation = new Triple(oldRotation.x + rotation.x + baseRotation.x, oldRotation.y + rotation.y + baseRotation.y, oldRotation.z + rotation.z  + baseRotation.z);
        SetBoneRotationAround(bone, pivotPoint, newRotation);
    }

    /**
     *
     * Adds some rotation to a single bone around some external pivot point.
     *
     * @param bone The bone to rotate
     * @param relPoint The relative point to rotate around
     * @param rotation The rotation as a triple representing euler angles in degrees.
     */
    public static void AddBoneRotationAroundRelative(Bone bone, Triple relPoint, Triple rotation) {
        if (!bone.hasElement()) return;
        Triple oldRotation = bone.getBaseRotation();
        Triple newRotation = new Triple(oldRotation.x + rotation.x, oldRotation.y + rotation.y, oldRotation.z + rotation.z);
        SetBoneRotationAroundRelative(bone, relPoint, newRotation);
    }


    /**
     *
     * Sets a bones rotation while also changing the rotation of all children bones it has to match. (this is what happens when rotating bones BlockBench animations)
     *
     * @param rootBone The bone you want to rotate.
     * @param rotation The rotation as an euler angle in degrees (xyz)
     */
    public static void SetBoneRotationWithChildren(Bone rootBone, Triple rotation) {
        Triple rootOrigin = rootBone.getRelOrigin();
        for (Bone bone : rootBone.getAllChildrenBones(true)) {
            if (!bone.hasElement()) {continue;}
            ItemDisplay itemDisplay = bone.getItemDisplay();
            Location currentLocation = itemDisplay.getLocation();
            Triple translation = new Triple(rootOrigin.x-currentLocation.x(),rootOrigin.y-currentLocation.y(),rootOrigin.z-currentLocation.z());
            SetBoneRotationAroundRelative(bone, translation, rotation);
        }
    }

    /**
     *
     * Adds a rotation to a bone while also changing the rotation of all children bones it has to match. (this is what happens when rotating bones BlockBench animations)
     *
     * @param rootBone The bone you want to rotate.
     * @param rotation The rotation as an euler angle in degrees (xyz)
     */
    public static void AddBoneRotationWithChildren(Bone rootBone, Triple rotation) {
        if (!rootBone.hasElement()) return;
        Triple oldRotation = rootBone.getBaseRotation();
        Triple newRotation = new Triple(oldRotation.x + rotation.x, oldRotation.y + rotation.y, oldRotation.z + rotation.z);
        SetBoneRotationWithChildren(rootBone, newRotation);
    }
}
