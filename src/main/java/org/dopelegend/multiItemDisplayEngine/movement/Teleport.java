package org.dopelegend.multiItemDisplayEngine.movement;

import org.bukkit.Location;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.TeleportEntityPacketData;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

public class Teleport {

    /**
     *
     * Teleports an entire itemDisplayGroup to some location (without pitch and yaw, use the Rotation package for that)
     *
     * @param itemDisplayGroup The itemDisplayGroup to teleport
     * @param location The location to teleport the itemDisplayGroup to.
     */
    public static void teleportItemDisplayGroup(ItemDisplayGroup itemDisplayGroup, Location location) {
        Triple relTeleport = Triple.difference(itemDisplayGroup.getPivotPoint(), location);
        teleportItemDisplayGroupRelative(itemDisplayGroup, relTeleport);
    }


    public static void teleportItemDisplayGroupRelative(ItemDisplayGroup itemDisplayGroup, Triple relativeCoordinates) {
        for (Bone bone : itemDisplayGroup.getRootBone().getAllChildrenBones(true)){
            teleportSingleBoneRelative(bone, relativeCoordinates);
        }
        itemDisplayGroup.setPivotPoint(
                itemDisplayGroup.getPivotPoint().add(relativeCoordinates.x, relativeCoordinates.y, relativeCoordinates.z));
    }

    /**
     *
     * Teleports a single bone without the children. This will only visually set the bone to that location, its actual position might not be.
     *
     * @param bone The bone to teleport
     * @param location The location to teleport the bone to
     */
    public static void teleportSingleBone(Bone bone, Location location) {
        if (!bone.hasElement() || bone.getPosition()==null) return;

        Triple relTeleport = Triple.difference(bone.getPosition().add(bone.getPosition()), location);

        // This is a duplicate of the function beneath, but to avoid having two checks for whether the bone has an element I have this duplicated.
        TeleportEntityPacketData teleportPacket = new TeleportEntityPacketData();
        teleportPacket.setEntityID(bone.getEntityID());
        teleportPacket.setRelCoords(relTeleport);
        bone.addPacket(teleportPacket);
        bone.setPosition(bone.getPosition().add(relTeleport));
    }

    /**
     *
     * Teleports a single bone without the children using a relative position.
     *
     * @param bone The bone to teleport
     * @param relCoords The relative coordinates to this bone.
     */
    public static void teleportSingleBoneRelative(Bone bone, Triple relCoords) {
        if (!bone.hasElement() || bone.getPosition()==null) return;

        TeleportEntityPacketData teleportPacket = new TeleportEntityPacketData();
        teleportPacket.setEntityID(bone.getEntityID());
        teleportPacket.setRelCoords(relCoords);
        bone.addPacket(teleportPacket);
        bone.setPosition(bone.getPosition().add(relCoords));
    }

    /**
     *
     * Teleports a bone to a location (like the teleportBone function), and all of its children by the same offset that the bone was teleported by. An example: The bone is at (0, 0, 0) and you input the location (0, 2, 0) in this situation all bones are teleported up by 2. This is what happens when changing a bone's position in a BlockBench animation.
     *
     * @param rootBone The bone that you want to teleport. (This doesn't have to be the rootBone of the itemDisplayGroup,
     *                but it's called rootBone because it is the rootBone in this context).
     * @param location The location you want to teleport the bone to.
     */
    public static void teleportBoneWithChildren(Bone rootBone, Location location) {
        if (rootBone.getPosition() == null) return;
        Triple relTeleport = Triple.difference(rootBone.getPosition().add(rootBone.getPosition()), location);
        for (Bone bone : rootBone.getAllChildrenBones(true)){
            teleportSingleBoneRelative(bone, relTeleport);
        }
    }

    /**
     *
     * Teleports a bone and all of its children to a relative position.
     * The relativeCoords are applied relative to each bone, so every bone will NOT be teleported to the same location,
     * but to the same relative position to themselves. This function in practice just calls the teleportBoneRelative function on all the bones.
     *
     * @param rootBone The bone that you want to teleport. (This doesn't have to be the rootBone of the itemDisplayGroup).
     * @param relativeCoords The relative position you want to offset the bone and all of its children by.
     */
    public static void teleportBoneRelativeWithChildren(Bone rootBone, Triple relativeCoords) {
        for (Bone bone : rootBone.getAllChildrenBones(true)){
            teleportSingleBoneRelative(bone, relativeCoords);
        }
    }
}
