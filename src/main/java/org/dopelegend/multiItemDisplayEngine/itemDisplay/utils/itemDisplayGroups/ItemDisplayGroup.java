package org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.lang3.tuple.MutablePair;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.bone.Bone;
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
    private IDGPacketHandler packetHandler;
    private IDGAnimationHandler animationHandler;

    static List<ItemDisplayGroup> allItemDisplayGroups = new ArrayList<>();


    //Class variables
    // TODO make these config based
    private int viewRangeSquared = 100*100;

    private List<Player> renderingPlayers = new ArrayList<>();
    private JsonObject rootJson;

    /**
     * This is the pivotPoint and therefore the location of this ItemDisplayGroup.
     */
    private Location pivotPoint;
    private UUID uuid;
    private Bone rootBone;
    private double yaw = 0;
    private double pitch = 0;
    private double roll = 0;
    private boolean destroyed;

    private String modelName;

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
        ensureHandlers();

        allItemDisplayGroups.add(this);

        if (pivotPoint == null || rootBone == null) {
            throw new IllegalArgumentException("Display groups cannot be intialized with null or empty values.");
        }
        this.pivotPoint = pivotPoint;
        this.rootBone = rootBone;
        this.uuid = UUID.randomUUID();
    }

    /**
     *
     * @param pivotPoint The location of the center which the itemDisplayGroup should be rotated around.
     * @param modelName The name of the model
     *
     */
    public ItemDisplayGroup(Location pivotPoint, String modelName) {
        ensureHandlers();

        allItemDisplayGroups.add(this);

        // TODO make a map with predefined ItemDisplayGroupBlueprints and check against that before recomputing everything.
        if (pivotPoint == null || modelName == null || modelName.isEmpty()) {
            throw new IllegalArgumentException("Display groups cannot be initialized with null or empty values.");
        }
        this.pivotPoint = pivotPoint;

        File file = FileReader.getModelFile(modelName);
        if(file == null) {
            throw new IllegalArgumentException("Display group got initialized with a model that doesn't exist.");
        }
        Bone rootBone = FileReader.getRootBone(file);
        if (rootBone == null) {
            this.rootBone = new Bone(new Triple(0, 0,0), new Triple(0, 0,0), null, new ArrayList<>(), UUID.randomUUID());
            MultiItemDisplayEngine.plugin.getLogger().severe("Failed to make rootBone (used empty RootBone instead) for ItemDisplayGroup using model: " + modelName);
            return;
        }

        this.rootBone = rootBone;
        this.uuid = UUID.randomUUID();
        this.rootJson = FileReader.getRootJsonObject(file);
        this.modelName = modelName;
    }

    private void ensureHandlers(){
        this.packetHandler = new IDGPacketHandler(this);
        this.animationHandler = new IDGAnimationHandler(this);
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
     * Teleports this itemDisplayGroup to some Location, this doesn't affect the rotation of the itemDisplayGroup
     *
     * @param location The location to teleport the itemDisplayGroup to.
     */
    public void teleport(Location location){
        Teleport.teleportItemDisplayGroup(this, location);
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
     *
     * Sets the rotation of the whole itemDisplayGroup, using 4x4 matrices.
     *
     * @param rotation The rotation as an Euler angle (3 angles) in degrees (360)
     */
    public void setRotation(Triple rotation){
        Rotate.SetRotationItemDisplayGroup(this, rotation);
        this.setRotationInEulerAngles(rotation);
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

    public UUID getUuid() {
        return uuid;
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

    public String getModelName() {
        return modelName;
    }

    public JsonObject getRootJson() {
        return rootJson;
    }

    public IDGPacketHandler getPacketHandler() {
        return packetHandler;
    }

    public IDGAnimationHandler getAnimationHandler() {
        return animationHandler;
    }
}
