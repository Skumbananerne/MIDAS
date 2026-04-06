package org.dopelegend.multiItemDisplayEngine.movement;

import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.ItemDisplayDataPacketData;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

public class TeleportSmooth {

    /**
     *
     * Teleports an itemDisplayGroup to a location smoothly (with interpolation). This doesn't affect the rotation, and therefore pitch and yaw is ignored.
     *
     * @param itemDisplayGroup The itemDisplayGroup to teleport.
     * @param location The location to teleport the itemDisplayGroup to.
     * @param interpolationDuration How long the teleport should take. (How long it should take for the itemDisplayGroup to get from start loc to end loc)
     */
    public static void TeleportItemDisplayGroupSmooth(ItemDisplayGroup itemDisplayGroup, Location location, int interpolationDuration){
        Triple relTranslation = Triple.difference(itemDisplayGroup.getPivotPoint(), location);
        TeleportItemDisplayGroupRelativeSmooth(itemDisplayGroup, relTranslation, interpolationDuration);
    }

    /**
     *
     * Teleports an itemDisplayGroup to a relative location smoothly (with interpolation).
     *
     * @param itemDisplayGroup The itemDisplayGroup to teleport.
     * @param relativeCoordinates The relative coordinates to teleport the itemDisplayGroup to.
     * @param interpolationDuration How long the teleport should take. (How long it should take for the itemDisplayGroup to get from start loc to end loc)
     */
    public static void TeleportItemDisplayGroupRelativeSmooth(ItemDisplayGroup itemDisplayGroup, Triple relativeCoordinates, int interpolationDuration){
        if (interpolationDuration <= 0){
            Teleport.teleportItemDisplayGroupRelative(itemDisplayGroup, relativeCoordinates);
        }
        for (Bone bone : itemDisplayGroup.getRootBone().getAllChildrenBones(true)){
            if(!bone.hasElement()) continue;
            ItemDisplayDataPacketData translationPacket = new ItemDisplayDataPacketData();
            translationPacket.setEntityID(bone.getEntityID());
            translationPacket.setTranslation(relativeCoordinates.toVector3f());
            translationPacket.setTransformationInterpolationDuration(interpolationDuration);
            bone.addPacket(translationPacket);
        }
        itemDisplayGroup.setPivotPoint(itemDisplayGroup.getPivotPoint().add(relativeCoordinates.x, relativeCoordinates.y, relativeCoordinates.z));
    }

    /**
     *
     * Smoothly teleports a bone to a location, and all of its children by the same offset that the bone was teleported by.
     * An example: The bone is at (0, 0, 0) and you input the location (0, 2, 0) in this situation all bones are teleported up by 2.
     * This is what happens when changing a bone's position in a BlockBench animation.
     *
     * @param rootBone The bone that you want to teleport. (This doesn't have to be the rootBone of the itemDisplayGroup).
     * @param location The location you want to teleport the bone to.
     * @param interpolationDuration How long it should take the bones to get from their start position to their end position.
     */
    public static void TeleportBoneWithChildrenSmooth(Bone rootBone, Location location,  int interpolationDuration){
        if (rootBone.getPosition() == null) return;
        Triple relCoords = Triple.difference(rootBone.getPosition().add(rootBone.getVisualOffset()), location);

        for (Bone bone : rootBone.getAllChildrenBones(true)){
            TeleportSingleBoneRelativeSmooth(bone, relCoords, interpolationDuration);
        }
    }

    /**
     *
     * Smoothly teleports and all of its children to a relative position. The relativeCoords are applied relative to each bone, so every bone will NOT be teleported to the same location, but to the same relative position to themselves.
     *
     * @param rootBone The bone that you want to teleport. (This doesn't have to be the rootBone of the itemDisplayGroup).
     * @param relativeCoords The relative position you want to offset the bone and all of its children by.
     * @param interpolationDuration How long it should take the bones to get from their start position to their end position.
     */
    public static void TeleportBoneRelativeWithChildrenSmooth(Bone rootBone, Triple relativeCoords,  int interpolationDuration) {
        for (Bone bone : rootBone.getAllChildrenBones(true)) {
            TeleportSingleBoneRelativeSmooth(bone, relativeCoords, interpolationDuration);
        }
    }

    /**
     *
     * Smoothly teleports a single bone to some absolute location (visually).
     *
     * @param bone The bone to teleport
     * @param location The location to teleport the bone to.
     * @param interpolationDuration How long the teleportation should take.
     */
    public static void TeleportSingleBoneSmooth(Bone bone, Location location, int interpolationDuration) {
        if (!bone.hasElement() || bone.getPosition()==null) return;

        Triple relCoords = Triple.difference(bone.getPosition().add(bone.getVisualOffset()), location);
        ItemDisplayDataPacketData translationPacket = new ItemDisplayDataPacketData();
        translationPacket.setEntityID(bone.getEntityID());
        translationPacket.setTranslation(relCoords.toVector3f());
        translationPacket.setTransformationInterpolationDuration(interpolationDuration);
        bone.addPacket(translationPacket);
    }

    /**
     *
     * Smoothly teleports a single bone to some relative position (visually).
     *
     * @param bone The bone to teleport
     * @param relCoords The relative coordinates to teleport the bone to.
     * @param interpolationDuration How long the teleportation should take.
     */
    public static void TeleportSingleBoneRelativeSmooth(Bone bone, Triple relCoords, int interpolationDuration) {
        if (!bone.hasElement() || bone.getPosition()==null) return;

        ItemDisplayDataPacketData translationPacket = new ItemDisplayDataPacketData();
        translationPacket.setEntityID(bone.getEntityID());
        translationPacket.setTranslation(relCoords.toVector3f());
        translationPacket.setTransformationInterpolationDuration(interpolationDuration);
        bone.addPacket(translationPacket);
    }
}
