package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import org.apache.commons.lang3.tuple.MutablePair;
import org.dopelegend.multiItemDisplayEngine.bone.Bone;
import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketSender;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.ItemDisplayDataPacketData;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.PacketData;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.TeleportEntityPacketData;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;

public class IDGPacketHandler {
    private final ItemDisplayGroup group;
    private final double teleportThreshold;

    public IDGPacketHandler(ItemDisplayGroup group) {
        this.group = group;
        teleportThreshold = this.group.getViewRangeSquared()/(3.0*3.0);
    }

    /**
     * Queues all packets from all bones in this itemDisplayGroup in the PacketSender.
     */
    public void queueAllPackets(){
        MutablePair<Boolean, Triple> outOfThreshold = new MutablePair<>(false, null);
        Map<UUID, TeleportEntityPacketData> teleportPacketsToSend = new HashMap<>();
        Map<UUID, ItemDisplayDataPacketData> interpolatedTransformationPacketsToSend = new HashMap<>();
        Map<UUID, ItemDisplayDataPacketData> instantTransformationPacketsToSend = new HashMap<>();


        for (Bone bone : group.getRootBone().getAllChildrenBones(true)){
            Triple relTeleportation = new Triple(0, 0, 0);
            Triple offset = bone.getVisualOffset();
            List<ItemDisplayDataPacketData> interpolatedTransformationPackets = new ArrayList<>();
            List<ItemDisplayDataPacketData> instantTransformationPackets = new ArrayList<>();

            // Sum all translations
            for (PacketData packetData : bone.getPackets()){
                if (packetData instanceof ItemDisplayDataPacketData transformationPacketData) {
//                    ItemDisplayDataPacketData currentData = transformationPacketData.clone();

                    if (transformationPacketData.getTransformationInterpolationDuration()<=0){
                        instantTransformationPackets.add(transformationPacketData);
                        continue;
                    }
                    interpolatedTransformationPackets.add(transformationPacketData);
                }
                else if (packetData instanceof TeleportEntityPacketData teleportEntityPacketData) {
                    relTeleportation.add(teleportEntityPacketData.getRelCoords());
                }
            }

            // Loop through interpolatedTransformationPackets (all transformation packets that need to be interpolated by the client) and multiply matrices.
            Matrix4f interpolatedTransformationMatrix = new Matrix4f();
            for (int i = interpolatedTransformationPackets.size()-1 ; i >= 0 ; i--){
                interpolatedTransformationMatrix.mul(interpolatedTransformationPackets.get(i).getRotMatrix());
            }

            // Same as above but for the instant ones (rotation only, teleport packets are used for translation)
            Matrix4f instantTransformationMatrix = new Matrix4f();
            for (int i = instantTransformationPackets.size()-1 ; i >= 0 ; i--){
                instantTransformationMatrix.mul(instantTransformationPackets.get(i).getRotMatrix());
            }


            bone.clearPackets();

            // Send teleport packet
            if (!relTeleportation.isEmpty()){
                TeleportEntityPacketData teleportPacket = new TeleportEntityPacketData();
                teleportPacket.setRelCoords(relTeleportation);
                teleportPacket.setEntityID(bone.getEntityID());
                teleportPacketsToSend.put(bone.getUUID(), teleportPacket);
            }

            // Make combined instant transformation packet, for instant rotation, pure instant translation is dealt with through teleportpackets.
            if (!instantTransformationMatrix.equals(new Matrix4f())){
                ItemDisplayDataPacketData bonePacket = new ItemDisplayDataPacketData();
                bonePacket.setRotMatrix(instantTransformationMatrix);
                bonePacket.setEntityID(bone.getEntityID());
                bonePacket.setInterpolationDelay(0);
                bonePacket.setTransformationInterpolationDuration(0);
                instantTransformationPacketsToSend.put(bone.getUUID(), bonePacket);
            }

            // Make combined interpolated transformation packet
            if (!interpolatedTransformationMatrix.equals(new Matrix4f())){
                ItemDisplayDataPacketData bonePacket = new ItemDisplayDataPacketData();
                bonePacket.setRotMatrix(interpolatedTransformationMatrix);
                bonePacket.setEntityID(bone.getEntityID());
                bonePacket.setInterpolationDelay(0);
                bonePacket.setTransformationInterpolationDuration(1);

                interpolatedTransformationPacketsToSend.put(bone.getUUID(), bonePacket);
            }


            // Check if the bone is in range
            Vector3f interpolatedTranslation = new Vector3f();
            interpolatedTransformationMatrix.getTranslation(interpolatedTranslation);
            // Commented out because it should always be zero.
 //           Vector3f instantTranslation = new Vector3f();
   //         instantTransformationMatrix.getTranslation(instantTranslation);

            Triple offsetWithTranslation = offset.clone().add(new Triple(interpolatedTranslation)
            //        .add(new Triple(instantTranslation))
            );


            if (!outOfThreshold.getLeft() && offsetWithTranslation.clone().squaredSum() >= teleportThreshold){
                outOfThreshold.setLeft(true);
                outOfThreshold.setRight(offset);
            }
        }

        // This is outside the for loop btw.

        // Teleport ItemDisplayGroup if necessary.
        if (outOfThreshold.getLeft()){
            Triple offset = outOfThreshold.getRight();
            // TODO Figure out if the pivotPoint is used as the actual location or the visual location.
            //  Since it's updated in the teleportSmooth function (used as a visual location) the line below seemingly needs to be commented out.
            //setPivotPoint(getPivotPoint().add(offset.x, offset.y, offset.z));
            // Teleport every bone
            for (Bone bone : group.getRootBone().getAllChildrenBones(true)){

                // Teleport packet
                TeleportEntityPacketData teleportPacket = teleportPacketsToSend.remove(bone.getUUID());
                if (teleportPacket == null){
                    teleportPacket = new TeleportEntityPacketData();
                    teleportPacket.setEntityID(bone.getEntityID());
                    teleportPacket.setRelCoords(new Triple(0, 0, 0));
                }
                teleportPacket.setRelCoords(teleportPacket.getRelCoords().add(offset));
                PacketSender.queuePacket(teleportPacket, bone.getRenderingPlayers());

                // Instant transformation packet
                Matrix4f translationMatrix = new Matrix4f();
                translationMatrix.translate(offset.clone().invert().toVector3f());

                ItemDisplayDataPacketData transformationPacket = instantTransformationPacketsToSend.remove(bone.getUUID());
                if (transformationPacket == null){
                    transformationPacket = new ItemDisplayDataPacketData();
                    transformationPacket.setRotMatrix(new Matrix4f());
                    transformationPacket.setEntityID(bone.getEntityID());
                    transformationPacket.setTransformationInterpolationDuration(0);
                    transformationPacket.setInterpolationDelay(0);
                }

                // This is needed because the instant transformation packets are used for rotation.
                transformationPacket.setRotMatrix(transformationPacket.getRotMatrix().mul(translationMatrix));
                PacketSender.queuePacket(transformationPacket, bone.getRenderingPlayers());


                // Interpolated transformation packet
                ItemDisplayDataPacketData interpolatedTransformationPacket = interpolatedTransformationPacketsToSend.remove(bone.getUUID());

                if (interpolatedTransformationPacket != null){
                    // TODO this needs to be changed back asp
                    interpolatedTransformationPacket.setTransformationInterpolationDuration(0);
                    PacketSender.queuePacket(interpolatedTransformationPacket, bone.getRenderingPlayers());
                }

                // Reset bone pos
                bone.setVisualOffset(bone.getVisualOffset().remove(offset));
                bone.setPosition(bone.getPosition().add(offset));
            }
        }

        // Send packets normally
        else {
            for (Bone bone : group.getRootBone().getAllChildrenBones(true)){
                ItemDisplayDataPacketData interpolatedPacket = interpolatedTransformationPacketsToSend.get(bone.getUUID());
                if (interpolatedPacket != null){
                    interpolatedPacket.setRotMatrix(interpolatedPacket.getRotMatrix().mul(bone.getTransformationMatrix()));
                    bone.setTransformationMatrix(new Matrix4f(interpolatedPacket.getRotMatrix()));
                    PacketSender.queuePacket(interpolatedPacket, bone.getRenderingPlayers());
                }
                ItemDisplayDataPacketData instantPacket = instantTransformationPacketsToSend.get(bone.getUUID());
                if (instantPacket != null){
                    instantPacket.setRotMatrix(instantPacket.getRotMatrix().mul(bone.getTransformationMatrix()));
                    bone.setTransformationMatrix(new Matrix4f(instantPacket.getRotMatrix()));
                    PacketSender.queuePacket(instantPacket, bone.getRenderingPlayers());
                }
                TeleportEntityPacketData teleportPacket = teleportPacketsToSend.get(bone.getUUID());
                if (teleportPacket != null){
                    PacketSender.queuePacket(teleportPacket, bone.getRenderingPlayers());
                }
            }
        }
    }

}
