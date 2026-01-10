package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.blockBench.FileReader;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.Animation;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.KeyFrame;
import org.dopelegend.multiItemDisplayEngine.movement.TeleportSmooth;
import org.dopelegend.multiItemDisplayEngine.movement.Teleport;
import org.dopelegend.multiItemDisplayEngine.rotation.Rotate;
import org.dopelegend.multiItemDisplayEngine.rotation.RotateSmooth;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.io.File;
import java.util.*;

/**
 * A group of ItemDisplays that are connected, that way you can move and rotate them around a center without their relative position to each other breaking.
 */
public class ItemDisplayGroup {

    public enum AnimationState{RUNNING, HOLDING, PAUSED, FREE}

    //Class variables
    private String groupUUID;
    private Location pivotPoint;
    private Bone rootBone;
    private double yaw;
    private double pitch;
    private double roll;
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
     * @param rootBone The bone at the top of the bone hiearchy, this is obviously also the bone with no parent and all other bones as children/grandchildren or any other generation under it.
     *
     */
    public ItemDisplayGroup(Location pivotPoint, Bone rootBone) {
        if (pivotPoint == null || rootBone == null) {
            throw new IllegalArgumentException("Display groups cannot be intialized with null or empty values.");
        }
        this.pivotPoint = pivotPoint;
        this.rootBone = rootBone;
        this.yaw = 0;
        this.pitch = 0;
        this.roll = 0;
        this.groupUUID = UUID.randomUUID().toString();
        this.animations = new HashMap<String, Animation>();
    }

    /**
     *
     * @param pivotPoint The location of the center which the itemDisplayGroup should be rotated around.
     * @param modelName The name of the model
     *
     */
    public ItemDisplayGroup(Location pivotPoint, String modelName) {
        // TODO make a map with predefined ItemDisplayGroupBlueprints and check against that before recomputing everything.
        if (pivotPoint == null || modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Display groups cannot be intialized with null or empty values.");
        }
        this.pivotPoint = pivotPoint;

        File file = FileReader.getModelFile(modelName);
        if(file == null) {
            throw new IllegalArgumentException("Display group got intialized with a model that doesn't exist.");
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
        this.groupUUID = UUID.randomUUID().toString();

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
     *
     * Spawn this itemDisplayGroup at its pivot point (the rotation doesn't matter)
     * //TODO make it so this doesn't always return true, or make it not return anything
     * @return Currently always returns true
     */
    public boolean spawn(){
        rootBone.spawn(new Triple(this.pivotPoint.getX(), this.pivotPoint.getY(), this.pivotPoint.getZ()), this.pivotPoint.getWorld());
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
        Map<Bone, List<KeyFrame>> keyframes = animation.getKeyFrames();

        int animationDuration = 0;

        for(Bone bone : keyframes.keySet()){
            int timestamp = Math.round(keyframes.get(bone).getLast().getTimeStamp() * 20);
            if(timestamp > animationDuration) animationDuration = timestamp;
        }

        Animation.LoopMode mode = animation.getLoopMode();
        Bone[] boneArray = keyframes.keySet().toArray(new Bone[0]);

        for (int i = 0; i < boneArray.length; i++) {
            List<KeyFrame> allBoneKeyFrames = keyframes.get(boneArray[i]);
            List<List<KeyFrame>> sortedKeyframes = new ArrayList<>();

            float lastTiming = 0;
            List<KeyFrame> tempKeyframeList = new ArrayList<>();
            for(KeyFrame keyFrame : allBoneKeyFrames){
                if(keyFrame.getTimeStamp() > lastTiming){
                    if(!tempKeyframeList.isEmpty()) sortedKeyframes.add(tempKeyframeList);
                    tempKeyframeList = new ArrayList<>();
                    lastTiming = keyFrame.getTimeStamp();
                    tempKeyframeList.add(keyFrame);
                }
                else {
                    tempKeyframeList.add(keyFrame);
                }
            }

            if(!tempKeyframeList.isEmpty()) sortedKeyframes.add(tempKeyframeList);

            int finalI = i;
            int finalAnimationDuration = animationDuration;
            new BukkitRunnable(){
                int j = 0;
                int currentDuration = 0;
                boolean stop;

                int tickDelayRotation = 0;
                int tickDelayPosition = 0;
                int tickDelayStop = 0;
                @Override
                public void run() {
                    if(tickDelayStop != 0){
                        tickDelayStop--;
                        return;
                    };
                    if(stop){
                        cancel();
                        if(mode == Animation.LoopMode.LOOP){
                            if(finalI == boneArray.length-1){
                                animationState = AnimationState.FREE;
                                resetBonesPosAndRot();
                                playAnimation(animationName);
                            }
                        }
                        else if (mode == Animation.LoopMode.ONCE){
                            animationState = AnimationState.FREE;
                            resetBonesPosAndRot();
                        }
                        else if (mode == Animation.LoopMode.HOLD){
                            animationState = AnimationState.HOLDING;
                        }
                        return;
                    }
                    if (j < sortedKeyframes.size()) {
                        if(tickDelayPosition == 0 || tickDelayRotation == 0){
                            int loopDuration = 0;
                            if (j < sortedKeyframes.size() - 1) {
                                KeyFrame keyFrame = sortedKeyframes.get(j).getFirst();
                                loopDuration = Math.round((sortedKeyframes.get(j + 1).getFirst().getTimeStamp() - keyFrame.getTimeStamp()) * 20);
                            }
                            boolean updated = false;
                            for (KeyFrame keyFrame : sortedKeyframes.get(j)) {
                                switch (keyFrame.getType()) {
                                    case "rotation":
                                        if(tickDelayRotation == 0){
                                            KeyFrame validKeyframe = null;
                                            for (int k = j + 1; k < sortedKeyframes.size(); k++) {
                                                if(validKeyframe != null) break;
                                                for(KeyFrame frame : sortedKeyframes.get(k)) {
                                                    if(frame.getType().equals("rotation")){
                                                        validKeyframe = frame;
                                                    }
                                                }
                                            }
                                            if(validKeyframe != null) {
                                                int smoothDuration = Math.round((validKeyframe.getTimeStamp() - keyFrame.getTimeStamp()) * 20);
                                                RotateSmooth.SetBoneRotationWithChildrenSmooth(boneArray[finalI], keyFrame.getXyz(), smoothDuration);

                                                tickDelayRotation = smoothDuration;
                                            }
                                            updated = true;
                                        }
                                        break;
                                    case "position":
                                        if(tickDelayPosition == 0){
                                            KeyFrame validKeyframe = null;
                                            for (int k = j + 1; k < sortedKeyframes.size(); k++) {
                                                if(validKeyframe != null) break;
                                                for(KeyFrame frame : sortedKeyframes.get(k)) {
                                                    if(frame.getType().equals("position")){
                                                        validKeyframe = frame;
                                                    }
                                                }
                                            }
                                            if(validKeyframe != null){
                                                Triple relPos = validKeyframe.getXyz().clone();
                                                relPos.remove(keyFrame.getXyz());
                                                relPos.divide(16);

                                                int smoothDuration = Math.round((validKeyframe.getTimeStamp() - keyFrame.getTimeStamp()) * 20);
                                                TeleportSmooth.TeleportBoneRelativeWithChildrenSmooth(boneArray[finalI], relPos, smoothDuration);

                                                tickDelayPosition = smoothDuration;
                                            }
                                            updated = true;
                                        }
                                        break;
                                    case "scale":
                                        //TODO SCALE
                                        break;
                                }
                            }

                            if(updated){
                                currentDuration += loopDuration;
                                j++;
                            }
                        }
                        if(tickDelayPosition > 0) tickDelayPosition--;
                        if(tickDelayRotation > 0) tickDelayRotation--;

                    } else {
                        tickDelayStop = finalAnimationDuration - currentDuration;
                        stop = true;
                    }

                }
            }.runTaskTimer(MultiItemDisplayEngine.plugin, 0L, 1);
        }
        
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
}
