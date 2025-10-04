package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.dopelegend.multiItemDisplayEngine.blockBench.Bone;
import org.dopelegend.multiItemDisplayEngine.blockBench.FileReader;
import org.dopelegend.multiItemDisplayEngine.utils.Uuid;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A group of ItemDisplays that are connected, that way you can move and rotate them around a center without their relative position to each other breaking.
 */
public class ItemDisplayGroup {

    //Class variables
    private Location pivotPoint;
    private Bone rootBone;
    private double yaw;
    private double pitch;
    private double roll;
    private String modelName;
    private String uuid;

    //Constructor
    /**
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
    }

    /**
     *
     * @param modelName The bone at the top of the bone hierarchy, this is obviously also the bone with no parent and all other bones as children/grandchildren or any other generation under it.
     *
     */
    public ItemDisplayGroup(String modelName) {
        if (modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Display groups cannot be intialized with null or empty values.");
        }

        File file = FileReader.getModelFile(modelName);
        if(file == null) {
            throw new IllegalArgumentException("Display group got intialized with a model that doesn't exist.");
        }
        this.modelName = modelName;
        this.rootBone = FileReader.getRootBone(file);
        this.yaw = 0;
        this.pitch = 0;
        this.roll = 0;

        ItemDisplayGroup.registerItemDisplayGroup(this);
    }

    /**
     *
     * @param other The ItemDisplayGroup that should be copied
     *
     */
    public ItemDisplayGroup(ItemDisplayGroup other) {
        this.pivotPoint = other.getPivotPoint();
        this.modelName = other.getModelName();
        this.rootBone = new Bone(other.getRootBone(), null);
        this.yaw = other.getYaw();
        this.pitch = other.getPitch();
        this.roll = other.getRoll();
        this.uuid = Uuid.getStringUuid();
    }

    public boolean Spawn(){
        rootBone.spawn(new Triple(this.pivotPoint.getX(), this.pivotPoint.getY(), this.pivotPoint.getZ()), this.pivotPoint.getWorld());
        ItemDisplayGroup.addSpawnedItemDisplayGroups(this);
        return true;
    }

    public Location getPivotPoint() {
        return pivotPoint;
    }

    public void setPivotPoint(Location pivotPoint) {
        this.pivotPoint = pivotPoint;
    }

    public Bone getRootBone() {
        return rootBone;
    }

    public void setRootBone(Bone rootBone) {
        this.rootBone = rootBone;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(double roll) {
        this.roll = roll;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getUuid() {
        return uuid;
    }

    public void RunAnimation(String animationName, boolean loop, double speed) {

    }

    private static List<Pair<ItemDisplayGroup, String>> registeredItemDisplayGroup = new ArrayList<>();
    private static final List<ItemDisplayGroup> spawnedItemDisplayGroup = new ArrayList<>();

    public static void registerItemDisplayGroup(ItemDisplayGroup itemDisplayGroup) {
        Pair<ItemDisplayGroup, String> tempItemDisplayGroup = Pair.of(itemDisplayGroup, itemDisplayGroup.getModelName());
        registeredItemDisplayGroup.add(tempItemDisplayGroup);
    }
    public static ItemDisplayGroup getItemDisplayGroup(Location pivotPoint, String modelName) {
        if (pivotPoint == null || modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("All values needed to find ItemDisplayGroup.");
        }

        ItemDisplayGroup foundGroup = null;
        for(Pair<ItemDisplayGroup, String> group : registeredItemDisplayGroup){
            if(Objects.equals(group.right(), modelName)){
                foundGroup = new ItemDisplayGroup(group.left());
            }
        }

        if(foundGroup != null){
            foundGroup.setPivotPoint(pivotPoint);
        }

        return foundGroup;
    }
    public static void resetRegisteredItemDisplayGroup(){
        registeredItemDisplayGroup = new ArrayList<>();
    }

    public static ItemDisplayGroup[] getAllSpawnedItemDisplayGroups(){
        return spawnedItemDisplayGroup.toArray(new ItemDisplayGroup[0]);
    }
    public static ItemDisplayGroup getByUuidSpawnedItemDisplayGroups(String uuid){
        for (ItemDisplayGroup itemDisplayGroup : spawnedItemDisplayGroup){
            if(Objects.equals(itemDisplayGroup.getUuid(), uuid)) return itemDisplayGroup;
        }
        return null;
    }
    public static void addSpawnedItemDisplayGroups(ItemDisplayGroup itemDisplayGroup){
        spawnedItemDisplayGroup.add(itemDisplayGroup);
    }

}
