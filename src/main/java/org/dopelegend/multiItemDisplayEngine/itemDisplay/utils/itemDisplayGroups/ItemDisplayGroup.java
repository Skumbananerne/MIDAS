package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.apache.commons.lang3.tuple.MutablePair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.blockBench.FileReader;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.Animation;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.KeyFrame;
import org.dopelegend.multiItemDisplayEngine.movement.TeleportSmooth;
import org.dopelegend.multiItemDisplayEngine.movement.Teleport;
import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketSender;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.ItemDisplayDataPacketData;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.PacketData;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.TeleportEntityPacketData;
import org.dopelegend.multiItemDisplayEngine.rotation.Rotate;
import org.dopelegend.multiItemDisplayEngine.rotation.RotateSmooth;
import org.dopelegend.multiItemDisplayEngine.utils.classes.EntityHandler;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.File;
import java.util.*;

/**
 * A group of ItemDisplays that are connected, that way you can move and rotate them around a center without their relative position to each other breaking.
 */
    public class ItemDisplayGroup {

    static List<ItemDisplayGroup> allItemDisplayGroups = new ArrayList<>();

    public enum AnimationState{RUNNING, HOLDING, PAUSED, FREE}

    //Class variables
    // TODO make these config based
    private int viewRangeSquared = 100*100;
    private final double teleportThreshold = viewRangeSquared/(3.0*3.0);

    private List<Player> renderingPlayers = new ArrayList<>();
    /**
     * This is the pivotPoint and therefore the location of this ItemDisplayGroup.
     */
    private Location pivotPoint;
    private UUID uuid;
    private Bone rootBone;
    private double yaw;
    private double pitch;
    private double roll;
    private boolean destroyed;
    private AnimationState animationState;

    /**
     * All the animations this ItemDisplayGroup has as a map where the key is a string name of the animation and the value is an Animation class.
     */
    private Map<String, Animation>  animations;

    //Constructor
    /**
     *
     * This constructor makes static itemDisplayGroups, as there's no animation references
     *
     * @param pivotPoint The location of the center which the itemDisplayGroup should be rotated around.
     * @param rootBone The bone at the top of the bone hierarchy, this is obviously also the bone with no parent and all other bones as children/grandchildren or any other generation under it.
     *
     */
    public ItemDisplayGroup(Location pivotPoint, Bone rootBone) {
        allItemDisplayGroups.add(this);

        if (pivotPoint == null || rootBone == null) {
            throw new IllegalArgumentException("Display groups cannot be intialized with null or empty values.");
        }
        this.pivotPoint = pivotPoint;
        this.rootBone = rootBone;
        this.yaw = 0;
        this.pitch = 0;
        this.roll = 0;
        this.animations = new HashMap<String, Animation>();
        this.uuid = UUID.randomUUID();
    }

    /**
     *
     * @param pivotPoint The location of the center which the itemDisplayGroup should be rotated around.
     * @param modelName The name of the model
     *
     */
    public ItemDisplayGroup(Location pivotPoint, String modelName) {
        allItemDisplayGroups.add(this);

        // TODO make a map with predefined ItemDisplayGroupBlueprints and check against that before recomputing everything.
        if (pivotPoint == null || modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Display groups cannot be intialized with null or empty values.");
        }
        this.pivotPoint = pivotPoint;

        File file = FileReader.getModelFile(modelName);
        if(file == null) {
            throw new IllegalArgumentException("Display group got initialized with a model that doesn't exist.");
        }
        Bone rootBone = FileReader.getRootBone(file);
        if (rootBone == null) {
            this.rootBone = new Bone(new Triple(0, 0,0), new Triple(0, 0,0), null, new ArrayList<>(), UUID.randomUUID().toString());
            MultiItemDisplayEngine.plugin.getLogger().severe("Failed to make rootBone (used empty RootBone instead) for ItemDisplayGroup using model: " + modelName);
            return;
        }

        this.rootBone = rootBone;
        this.yaw = 0;
        this.pitch = 0;
        this.roll = 0;
        this.uuid = UUID.randomUUID();

        JsonObject rootJson = FileReader.getRootJsonObject(file);
        if (rootJson == null) {
            this.animations = new HashMap<>();
            MultiItemDisplayEngine.plugin.getLogger().severe("Failed to make animations (used empty animation list instead) for ItemDisplayGroup using model: " + modelName);
            return;
        }

        Map<String, Animation> animations = new HashMap<>();

        JsonArray animationsArray =  rootJson.getAsJsonArray("animations");
        if (animationsArray == null){
            this.animations = new HashMap<>();
            return;
        }
        for (JsonElement element : animationsArray) {
            if (element instanceof JsonObject animation) {
                animations.put(animation.get("name").getAsString(), new Animation(animation, rootBone));
            }
        }

        this.animations = animations;
    }

    /**
     * Queues all packets from all bones in this itemDisplayGroup in the PacketSender.
     */
    public void queueAllPackets(){
        MutablePair<Boolean, Triple> outOfThreshold = new MutablePair<>(false, null);
        Map<Bone, TeleportEntityPacketData> teleportPacketsToSend = new HashMap<>();
        Map<Bone, ItemDisplayDataPacketData> interpolatedTransformationPacketsToSend = new HashMap<>();
        Map<Bone, ItemDisplayDataPacketData> instantTransformationPacketsToSend = new HashMap<>();


        for (Bone bone : rootBone.getAllChildrenBones(true)){
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
//                    if(transformationPacketData.getTransformationInterpolationDuration() > 1){
//                        ItemDisplayDataPacketData additionalData = new ItemDisplayDataPacketData();
//
//                        Vector3f currentVector = new Vector3f();
//                        currentData.setTranslation(transformationPacketData.getTranslation().div(
//                                transformationPacketData.getTransformationInterpolationDuration(),
//                                currentVector
//                        ));
//                        currentData.setTransformationInterpolationDuration(1);
//
//                        Vector3f additionalVector = new Vector3f();
//                        additionalData.setTranslation(transformationPacketData.getTranslation().sub(currentData.getTranslation(), additionalVector));
//                        additionalData.setTransformationInterpolationDuration(transformationPacketData.getTransformationInterpolationDuration() - 1);
//
//                        addedData.add(additionalData);
//                    }
//                    relTranslation.add(new Triple(currentData.getTranslation()));
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
                teleportPacketsToSend.put(bone, teleportPacket);
            }

//            MultiItemDisplayEngine.plugin.getLogger().info("Offset: "+offset);
//            MultiItemDisplayEngine.plugin.getLogger().info("relTranslation "+relTranslation);

            Vector3f interpolatedTranslation = new Vector3f();
            interpolatedTransformationMatrix.getTranslation(interpolatedTranslation);

            Vector3f instantTranslation = new Vector3f();
            instantTransformationMatrix.getTranslation(instantTranslation);

            Triple offsetWithTranslation = offset.clone().add(new Triple(interpolatedTranslation).add(new Triple(instantTranslation)));


            // Make position reset if it's outside teleportThreshold
            if (!outOfThreshold.getLeft() && offsetWithTranslation.clone().squaredSum() >= teleportThreshold){
                outOfThreshold.setLeft(true);
                outOfThreshold.setRight(offsetWithTranslation);
//
//                TeleportEntityPacketData teleportResetPacket = new TeleportEntityPacketData();
//                teleportResetPacket.setRelCoords(offset.clone());
//                teleportResetPacket.setEntityID(bone.getEntityID());
//
//                ItemDisplayDataPacketData translationPacket = new ItemDisplayDataPacketData();
//                translationPacket.setTranslation(dest);
//                translationPacket.setEntityID(bone.getEntityID());
//                translationPacket.setInterpolationDelay(0);
//                translationPacket.setTransformationInterpolationDuration(0);
//
//                PacketSender.queuePacket(translationPacket, bone.getRenderingPlayers());
//                PacketSender.queuePacket(teleportResetPacket, bone.getRenderingPlayers());
//
//                bone.setVisualOffset(new Triple(0, 0, 0));
//
//                continue;
            }

            // Make combined instant transformation packet, for instant rotation, pure instant translation is dealt with through teleportpackets.
            if (!instantTransformationMatrix.equals(new Matrix4f())){
                ItemDisplayDataPacketData bonePacket = new ItemDisplayDataPacketData();
//                instantTransformationMatrix.mul(bone.getTransformationMatrix());
                bonePacket.setRotMatrix(instantTransformationMatrix);
                bonePacket.setEntityID(bone.getEntityID());
                bonePacket.setInterpolationDelay(0);
                bonePacket.setTransformationInterpolationDuration(0);

//                bone.setTransformationMatrix(instantTransformationMatrix);
                instantTransformationPacketsToSend.put(bone, bonePacket);

            }

            // Make combined interpolated transformation packet
            if (!interpolatedTransformationMatrix.equals(new Matrix4f())){
                ItemDisplayDataPacketData bonePacket = new ItemDisplayDataPacketData();
//                interpolatedTransformationMatrix.mul(bone.getTransformationMatrix());
                bonePacket.setRotMatrix(interpolatedTransformationMatrix);
                bonePacket.setEntityID(bone.getEntityID());
                bonePacket.setInterpolationDelay(0);
                bonePacket.setTransformationInterpolationDuration(1);

//                bone.setTransformationMatrix(interpolatedTransformationMatrix);
                interpolatedTransformationPacketsToSend.put(bone, bonePacket);
            }
        }

        // Teleport ItemDisplayGroup if necessary.
        if (outOfThreshold.getLeft()){
            Triple offset = outOfThreshold.getRight();
            setPivotPoint(getPivotPoint().add(offset.x, offset.y, offset.z));
            // Teleport every bone
            for (Bone bone : rootBone.getAllChildrenBones(true)){

                // Teleport packet
                TeleportEntityPacketData teleportPacket = teleportPacketsToSend.get(bone);
                if (teleportPacket == null){
                    teleportPacket = new TeleportEntityPacketData();
                    teleportPacket.setEntityID(bone.getEntityID());
                    teleportPacket.setRelCoords(new Triple(0, 0, 0));

                }
                teleportPacket.setRelCoords(teleportPacket.getRelCoords().add(offset));
                PacketSender.queuePacket(teleportPacket, bone.getRenderingPlayers());

                MultiItemDisplayEngine.plugin.getLogger().info("------------Teleport------------");
                MultiItemDisplayEngine.plugin.getLogger().info(teleportPacket.getRelCoords().toString());
                MultiItemDisplayEngine.plugin.getLogger().info("-------------------------------");

                // Instant transformation packet

                Matrix4f translationMatrix = new Matrix4f();
                translationMatrix.translate(offset.clone().invert().toVector3f());

                ItemDisplayDataPacketData transformationPacket = instantTransformationPacketsToSend.get(bone);
                if (transformationPacket == null){
                    transformationPacket = new ItemDisplayDataPacketData();
                    transformationPacket.setRotMatrix(new Matrix4f());
                    transformationPacket.setEntityID(bone.getEntityID());
                    transformationPacket.setTransformationInterpolationDuration(0);
                    transformationPacket.setInterpolationDelay(0);
                }

                PacketSender.queuePacket(transformationPacket, bone.getRenderingPlayers());

                MultiItemDisplayEngine.plugin.getLogger().info("------------Instant------------");
                MultiItemDisplayEngine.plugin.getLogger().info(transformationPacket.getTranslation().toString());
                MultiItemDisplayEngine.plugin.getLogger().info(transformationPacket.getRotMatrix().toString());
                MultiItemDisplayEngine.plugin.getLogger().info("-------------------------------");

                // Interpolated transformation packet
                ItemDisplayDataPacketData interpolatedTransformationPacket = interpolatedTransformationPacketsToSend.get(bone);
                if (interpolatedTransformationPacket == null){
                    interpolatedTransformationPacket = new ItemDisplayDataPacketData();
                    interpolatedTransformationPacket.setRotMatrix(new Matrix4f());
                    interpolatedTransformationPacket.setEntityID(bone.getEntityID());
                    interpolatedTransformationPacket.setInterpolationDelay(0);
                    interpolatedTransformationPacket.setTransformationInterpolationDuration(1);
                }
                Matrix4f interpolatedTranslation = new Matrix4f(interpolatedTransformationPacket.getRotMatrix());

                interpolatedTransformationPacket.setRotMatrix(interpolatedTranslation.mul(transformationPacket.getRotMatrix()));
                PacketSender.queuePacket(interpolatedTransformationPacket, bone.getRenderingPlayers());
                MultiItemDisplayEngine.plugin.getLogger().info("------------Interpolated------------");
                MultiItemDisplayEngine.plugin.getLogger().info(interpolatedTransformationPacket.getTranslation().toString());
                MultiItemDisplayEngine.plugin.getLogger().info(interpolatedTransformationPacket.getRotMatrix().toString());
                MultiItemDisplayEngine.plugin.getLogger().info("-------------------------------");
                // Reset bone pos
                bone.setVisualOffset(bone.getVisualOffset().remove(offset));
                bone.setPosition(bone.getPosition().add(offset));

                MultiItemDisplayEngine.plugin.getLogger().info("------------Visual offset------------");
                MultiItemDisplayEngine.plugin.getLogger().info(bone.getVisualOffset().toString());
                MultiItemDisplayEngine.plugin.getLogger().info("-------------------------------");
                MultiItemDisplayEngine.plugin.getLogger().info("------------Position------------");
                MultiItemDisplayEngine.plugin.getLogger().info(bone.getPosition().toString());
                MultiItemDisplayEngine.plugin.getLogger().info("-------------------------------");

                Bukkit.broadcastMessage("Reset");
            }
        }
        // Send packets normally
        else {
            for (Bone bone : rootBone.getAllChildrenBones(true)){
                ItemDisplayDataPacketData interpolatedPacket = interpolatedTransformationPacketsToSend.get(bone);
                if (interpolatedPacket != null){
                    interpolatedPacket.setRotMatrix(interpolatedPacket.getRotMatrix().mul(bone.getTransformationMatrix()));
                    bone.setTransformationMatrix(new Matrix4f(interpolatedPacket.getRotMatrix()));
                    PacketSender.queuePacket(interpolatedPacket, bone.getRenderingPlayers());
                    MultiItemDisplayEngine.plugin.getLogger().info("------------Normal teleport------------");
                    MultiItemDisplayEngine.plugin.getLogger().info(interpolatedPacket.getTranslation().toString());
                    MultiItemDisplayEngine.plugin.getLogger().info("-------------------------------");
                }
                ItemDisplayDataPacketData instantPacket = instantTransformationPacketsToSend.get(bone);
                if (instantPacket != null){
                    instantPacket.setRotMatrix(instantPacket.getRotMatrix().mul(bone.getTransformationMatrix()));
                    bone.setTransformationMatrix(new Matrix4f(instantPacket.getRotMatrix()));
                    PacketSender.queuePacket(instantPacket, bone.getRenderingPlayers());
                }
                TeleportEntityPacketData teleportPacket = teleportPacketsToSend.get(bone);
                if (teleportPacket != null){
                    PacketSender.queuePacket(teleportPacket, bone.getRenderingPlayers());
                }
            }
        }
    }

    /**
     *
     * Spawn this itemDisplayGroup at its pivot point (the rotation doesn't matter)
     * //TODO make it so this doesn't always return true, or make it not return anything
     * @return Currently always returns true or throws an error
     */
    public boolean spawn(){
        this.rootBone.syncPositionToDisplayGroup(new Triple(pivotPoint));
        PacketUpdater.addActiveItemDisplayGroup(this);
        return true;
    }

    public boolean destroy(){
        allItemDisplayGroups.remove(this);
        PacketUpdater.removeActiveItemDisplayGroup(this);
        List<Player> renderingPlayersClone = new ArrayList<>(renderingPlayers);
        for(Player player : renderingPlayersClone){
            EntityHandler.getEntityHandler(player.getUniqueId()).removeActiveItemDisplayGroup(uuid);
            unrender(player);
        }
        destroyed = true;
        return true;
    }

    public void resetBonesPosAndRot(){
        rootBone.resetLocation(this, true);
    }

    /**
     *
     * Makes this itemDisplayGroup play an animation with the given name.
     *
     * @param animationName The name of the animation, this is the same as it's called in BlockBench.
     * @return False if the animation couldn't be found, or there's already an animation running, otherwise true.
     */
    public boolean playAnimation(String animationName){
        if (!this.animations.containsKey(animationName)) return false;
        if (animationState == AnimationState.RUNNING) return false;

        if(animationState == AnimationState.PAUSED || animationState == AnimationState.HOLDING){
            resetBonesPosAndRot();
        }

        animationState = AnimationState.RUNNING;

        Animation animation = this.animations.get(animationName);
        Map<Bone, List<KeyFrame>> keyframeSet = animation.getKeyFrames();

        Animation.LoopMode mode = animation.getLoopMode();
        Bone[] boneArray = keyframeSet.keySet().toArray(new Bone[0]);

        for (int i = 0; i < boneArray.length; i++) {
            List<KeyFrame> keyFrames = keyframeSet.get(boneArray[i]);

            for (int j = 0; j < keyFrames.size(); j++) {
                KeyFrame keyFrame = keyFrames.get(j);
                int finalJ = j;
                int finalI = i;
                new BukkitRunnable(){
                    @Override
                    public void run() {
                        KeyFrame validKeyframe = null;
                        switch (keyFrame.getType()) {
                            case "rotation":
                                for (int k = finalJ + 1; k < keyFrames.size(); k++) {
                                    if (validKeyframe != null) break;
                                    if (keyFrames.get(k).getType().equals("rotation")) {
                                        validKeyframe = keyFrames.get(k);
                                    }
                                }
                                if (validKeyframe != null) {
                                    int smoothDuration = Math.round((validKeyframe.getTimeStamp() - keyFrame.getTimeStamp()) * 20);
                                    RotateSmooth.SetBoneRotationWithChildrenSmooth(boneArray[finalI], validKeyframe.getXyz(), smoothDuration);
                                }
                                break;
                            case "position":
                                for (int k = finalJ + 1; k < keyFrames.size(); k++) {
                                    if (validKeyframe != null) break;
                                    if (keyFrames.get(k).getType().equals("position")) {
                                        validKeyframe = keyFrames.get(k);
                                    }
                                }

                                if (validKeyframe != null) {
                                    Triple relPos = validKeyframe.getXyz().clone();
                                    relPos.remove(keyFrame.getXyz());
                                    relPos.divide(16);

                                    int smoothDuration = Math.round((validKeyframe.getTimeStamp() - keyFrame.getTimeStamp()) * 20);
                                    TeleportSmooth.TeleportBoneRelativeWithChildrenSmooth(boneArray[finalI], relPos, smoothDuration);
                                }
                                break;
                            case "scale":
                                //TODO SCALE
                                break;
                        }
                    }
                }.runTaskLater(MultiItemDisplayEngine.plugin, Math.round(keyFrame.getTimeStamp() * 20));
            }
        }

        new BukkitRunnable(){
            @Override
            public void run() {
                if (mode == Animation.LoopMode.LOOP) {
                    animationState = AnimationState.FREE;
                    //resetBonesPosAndRot();
                    playAnimation(animationName);
                } else if (mode == Animation.LoopMode.ONCE) {
                    animationState = AnimationState.FREE;
                    //resetBonesPosAndRot();
                } else if (mode == Animation.LoopMode.HOLD) {
                    animationState = AnimationState.HOLDING;
                }
            }
        }.runTaskLater(MultiItemDisplayEngine.plugin, Math.round(animation.getLength() * 20) + 1);

        return true;
    }

    /**
     *
     * Teleports this itemDisplayGroup to some Location, this doesn't affect the rotation of the itemDisplayGroup
     *
     * @param location The location to teleport the itemDisplayGroup to.
     */
    public void teleport(Location location){
        Teleport.teleportItemDisplayGroup(this, location);
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     *
     * Adds some player to the renderingPlayers list
     *
     * @param player the player to add
     */
    private void addRenderingPlayer(Player player){
        renderingPlayers.add(player);
    }

    private void removeRenderingPlayer(Player player){
        renderingPlayers.remove(player);
    }

    /**
     *
     * Renders this itemDisplayGroup for a player, this will be called automatically when players enter the view range.
     *
     * @param player The player to render the itemDisplayGroup for.
     */
    public void render(Player player) {
        this.getRootBone().render(new Triple(this.pivotPoint), player);
        addRenderingPlayer(player);
    }

    /**
     *
     * Unrenders this itemDisplayGroup for a player.
     *
     * @param player The player to unrender this itemDisplayGroup for.
     */
    public void unrender(Player player) {
        this.getRootBone().unrender(player);
        removeRenderingPlayer(player);
    }

    /**
     *
     * Teleports this itemDisplayGroup by coordinates (x, y, z) relative to the pivotPoint of this itemDisplayGroup.
     *
     * @param relativeCoordinates A triple containing the relative coordinates you want to teleport the ItemDisplayGroup to.
     */
    public void teleportRelative(Triple relativeCoordinates){
        Teleport.teleportItemDisplayGroupRelative(this, relativeCoordinates);
    }

    /**
     *
     * Teleports this itemDisplayGroup to some Location smoothly (interpolates the teleport), this doesn't affect the rotation of the itemDisplayGroup
     *
     * @param location The location to teleport the itemDisplayGroup to.
     * @param teleportDuration How long the teleport should last (how long the ItemDisplayGroup takes to get from start loc to end loc.)
     */
    public void teleportSmooth(Location location, int teleportDuration){
        TeleportSmooth.TeleportItemDisplayGroupSmooth(this, location, teleportDuration);
    }

    /**
     *
     * Smoothly (interpolates) teleports this itemDisplayGroup by coordinates (x, y, z) relative to the pivotPoint of this itemDisplayGroup.
     *
     * @param relativeCoordinates A triple containing the relative coordinates you want to teleport the ItemDisplayGroup to.
     * @param teleportDuration How long the teleport should last (how long the ItemDisplayGroup takes to get from start loc to end loc.)
     */
    public void teleportRelativeSmooth(Triple relativeCoordinates, int teleportDuration){
        TeleportSmooth.TeleportItemDisplayGroupRelativeSmooth(this, relativeCoordinates, teleportDuration);
    }

    /**
     *
     * Sets the rotation of the whole itemDisplayGroup, using 4x4 matrices.
     *
     * @param rotation The rotation as an euler angle (3 angles) in degrees (360)
     */
    public void setRotation(Triple rotation){
        Rotate.SetRotationItemDisplayGroup(this, rotation);
        this.setRotationInEulerAngles(rotation);
    }

    /**
     *
     * Adds some rotation to the whole itemDisplayGroup, using 4x4 matrices.
     *
     * @param rotation The rotation to add as an euler angle (3 angles) in degrees (360)
     */
    public void addRotation(Triple rotation){
        Rotate.AddRotationItemDisplayGroup(this, rotation);
        Triple oldRotation = this.getRotationInEulerAngles();
        this.setRotationInEulerAngles(new Triple(oldRotation.x+ rotation.x, oldRotation.y+rotation.y, oldRotation.z+rotation.z));
    }

    /**
     *
     * Sets the rotation smoothly of the whole itemDisplayGroup, using 4x4 matrices.
     *
     * @param rotation The rotation as an euler angle (3 angles) in degrees (360)
     * @param interpolationDuration How long it should take the itemDisplayGroup to get from its start rotation to the rotation given.
     */
    public void setRotationSmooth(Triple rotation, int interpolationDuration){
        RotateSmooth.SetRotationItemDisplayGroupSmooth(this, rotation, interpolationDuration);
        this.setRotationInEulerAngles(rotation);
    }

    public void setViewRange(int viewRange){
        this.viewRangeSquared = viewRange*viewRange;
    }

    public int getViewRange(){
        double distanceSquared = viewRangeSquared;
        double distance = Math.sqrt(distanceSquared);
        return (int) distance;
    }

    public int getViewRangeSquared(){
        return viewRangeSquared;
    }

    /**
     *
     * Smoothly adds some rotation to the whole itemDisplayGroup, using 4x4 matrices.
     *
     * @param rotation The rotation to add as an euler angle (3 angles) in degrees (360)
     */
    public void addRotationSmooth(Triple rotation, int interpolationDuration){
        ItemDisplayGroup itemDisplayGroup = this;
        Triple subdividedRotation = rotation.clone().divide(interpolationDuration);
        new BukkitRunnable() {

            int counter = 0;

            @Override
            public void run() {
                if(counter >= interpolationDuration){
                    this.cancel();
                    return;
                }
                counter++;
                RotateSmooth.AddRotationItemDisplayGroupSmooth(itemDisplayGroup, subdividedRotation, 1);
                Triple oldRotation = itemDisplayGroup.getRotationInEulerAngles();
                itemDisplayGroup.setRotationInEulerAngles(new Triple(oldRotation.x+ subdividedRotation.x, oldRotation.y+subdividedRotation.y, oldRotation.z+subdividedRotation.z));
            }
        }.runTaskTimer(MultiItemDisplayEngine.plugin, 0L, 1);
    }

    /**
     *
     * Check whether this ItemDisplayGroup is marked as destroyed. A destroyed mean among other things that the model isn't rendered.
     *
     * @return Whether the itemDisplayGroup is destroyed.
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    public double GetYaw() {
        return this.yaw;
    }

    public double GetPitch() {
        return this.pitch;
    }

    public double GetRoll() {
        return this.roll;
    }

    /**
     * Gets the rotation as a triple in the following order RPY (Yaw, Pitch, Roll)
     * @return The triple holding the euler angles (in degrees)
     */
    public Triple getRotationInEulerAngles() {
        return new Triple(this.yaw, this.pitch, this.roll);
    }

    /**
     * Sets the stored rotation as a triple in the following order RPY (Yaw, Pitch, Roll)
     *
     * @param rotation The rotation as an euler angle in the following order (Yaw, Pitch, Roll) in degrees.
     */
    private void setRotationInEulerAngles(Triple rotation) {
        this.yaw = rotation.x;
        this.pitch = rotation.y;
        this.roll = rotation.z;
    }

    public void setPivotPoint(Location pivotPoint) {
        this.pivotPoint = pivotPoint;
    }

    public Location getPivotPoint() {
        return this.pivotPoint;
    }

    public Bone getRootBone() {return this.rootBone;}

    public AnimationState getAnimationState() {return this.animationState;}

    public void setAnimationState(AnimationState animationState) {
        this.animationState = animationState;
    }

    public static List<UUID> getAllUuids(){
        List<UUID> out = new ArrayList<>();
        for(ItemDisplayGroup group : allItemDisplayGroups){
            out.add(group.getUuid());
        }

        return out;
    }
    public static ItemDisplayGroup getItemDisplayGroup(UUID uuid){
        for(ItemDisplayGroup group : allItemDisplayGroups){
            if(group.getUuid().equals(uuid)) return group;
        }

        return null;
    }

    public static List<ItemDisplayGroup> getAllItemDisplayGroups(){
        return allItemDisplayGroups;
    }
}
