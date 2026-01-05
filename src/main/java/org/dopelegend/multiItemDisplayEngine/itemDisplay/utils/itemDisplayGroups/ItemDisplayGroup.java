package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.blockBench.FileReader;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.Animation;
import org.dopelegend.multiItemDisplayEngine.movement.SmoothTeleport;
import org.dopelegend.multiItemDisplayEngine.movement.Teleport;
import org.dopelegend.multiItemDisplayEngine.rotation.Rotate;
import org.dopelegend.multiItemDisplayEngine.rotation.RotateSmooth;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A group of ItemDisplays that are connected, that way you can move and rotate them around a center without their relative position to each other breaking.
 */
public class ItemDisplayGroup {

    //Class variables
    private String groupUUID;
    private Location pivotPoint;
    private Bone rootBone;
    private double yaw;
    private double pitch;
    private double roll;
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
            this.rootBone = new Bone(new Triple(0, 0,0), null, new ArrayList<>(), UUID.randomUUID().toString());
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

    /**
     *
     * Makes this itemDisplayGroup play an animation with the given name.
     *
     * @param animationName The name of the animation, this is the same as it's called in BlockBench.
     * @return False if the animation couldn't be found, true if it could.
     */
    public boolean playAnimation(String animationName){
        if (!this.animations.containsKey(animationName)){return false;}
        // TODO make this work

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
        SmoothTeleport.TeleportItemDisplayGroupSmooth(this, location, teleportDuration);
    }

    /**
     *
     * Smoothly (interpolates) teleports this itemDisplayGroup by coordinates (x, y, z) relative to the pivotPoint of this itemDisplayGroup.
     *
     * @param relativeCoordinates A triple containing the relative coordinates you want to teleport the ItemDisplayGroup to.
     * @param teleportDuration How long the teleport should last (how long the ItemDisplayGroup takes to get from start loc to end loc.)
     */
    public void teleportRelativeSmooth(Triple relativeCoordinates, int teleportDuration){
        SmoothTeleport.TeleportItemDisplayGroupRelativeSmooth(this, relativeCoordinates, teleportDuration);
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
        RotateSmooth.AddRotationItemDisplayGroupSmooth(this, rotation, interpolationDuration);
        Triple oldRotation = this.getRotationInEulerAngles();
        this.setRotationInEulerAngles(new Triple(oldRotation.x+ rotation.x, oldRotation.y+rotation.y, oldRotation.z+rotation.z));
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



//    public void AddRotation2D(double yaw, int ticks) {
//        for (ItemDisplay itemDisplay : itemDisplayList) {
//            itemDisplay.setTeleportDuration(ticks);
//            itemDisplay.teleport(Rotate2D.AddRotation(yaw, this.centerOfRotation, itemDisplay.getLocation().clone()));
//        }
//    }
//
//    public void SetRotation2D(double yaw, int ticks) {
//        for (ItemDisplay itemDisplay : itemDisplayList) {
//            itemDisplay.setTeleportDuration(ticks);
//            itemDisplay.teleport(Rotate2D.SetRotation(yaw, this.centerOfRotation, itemDisplay.getLocation().clone(), 0));
//        }
//    }
//
//    public void SetRotation3D(double yaw, double pitch, double roll, int ticks) {
//        this.yaw = yaw;
//        this.pitch = pitch;
//        this.roll = roll;
//
//        for (ItemDisplay itemDisplay : itemDisplayList) {
//            itemDisplay.setTeleportDuration(ticks);
//            itemDisplay.teleport(Rotate3D.rotateAroundCenter(itemDisplay, this.centerOfRotation, yaw, pitch, roll));
//        }
//    }
    public void RunAnimation(String animationName, boolean loop, double speed) {

    }
}
