package org.dopelegend.multiItemDisplayEngine.blockBench;

import net.minecraft.world.level.biome.BiomeManager;
import org.bukkit.*;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.movement.Teleport;
import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketCreator;
import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketSender;
import org.dopelegend.multiItemDisplayEngine.packetHandler.packets.ItemDisplayPacketData;
import org.dopelegend.multiItemDisplayEngine.rotation.Rotate;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * A single bone of a model, these are the smallest parts that models are split into each bone is equal to either one or zero item displays.
 * Each bone has a parent bone (except the root bone), as well as children bones a bone is not guaranteed to have children bones.
 *
 */
public class Bone {

    /**
     * The relative pivot point of this bone in 16ths of a block compared to the center of the ItemDisplayGroup, it's linked to.
     */
    private Triple relPivot;
    /**
     * This is the base rotation of this bone found from the .bbmodel file. This usually isn't what you want to change (look at currentRotation)
     */
    private Triple baseRotation;
    /**
     * This is the currentRotation of this bone as an euler angle in degrees, note that changing this won't actually change the rotation it will simply update the value.
     */
    private Triple currentRotation;

    /**
     * The entityID of the itemDisplay this is linked to.
     */
    private int entityID;
    private ItemDisplay itemDisplay;
    private String UUID = "";
    private List<Bone> childrenBones;
    private Bone parentBone;
    private String modelName;
    private ItemStack displayedItem;
    private ItemDisplay pivotPointDisplay;

    /**
     * The world coordinates of this position.
     */
    private Triple position;

    boolean hasElement = false;
    List<Player> renderingPlayers = new ArrayList<>();

    /**
     *
     * When this constructor is used we assume the bone doesn't have an element.
     *
     * @param relPivot The origin represented by 3 doubles in the form of a triple.
     * @param parentBone Parent of this bone
     * @param childrenBones All children direct child bones from this bone
     * @param UUID UUID of this bone
     */
    public Bone(Triple relPivot, Triple baseRotation, Bone parentBone, List<Bone> childrenBones, String UUID) {
        this.relPivot = relPivot;
        this.UUID = UUID;
        this.childrenBones = childrenBones;
        this.parentBone = parentBone;
        this.hasElement = false;
        this.baseRotation = baseRotation;
        this.currentRotation = baseRotation;
    }

    /**
     *
     * When this constructor is used we assume the bone have an element.
     *
     * @param relPivot The origin represented by 3 doubles in the form of a triple.
     * @param parentBone Parent of this bone
     * @param childrenBones All children direct child bones from this bone
     * @param UUID UUID of this bone
     */
    public Bone(Triple relPivot, Triple baseRotation, Bone parentBone, List<Bone> childrenBones, String UUID, String modelName) {
        this.relPivot = relPivot;
        this.UUID = UUID;
        this.childrenBones = childrenBones;
        this.parentBone = parentBone;
        this.hasElement = true;
        this.modelName = modelName;
        this.baseRotation = baseRotation;
        this.currentRotation = baseRotation;
        this.displayedItem = createDisplayedItem();
        this.entityID = Bukkit.getUnsafe().nextEntityId();
    }

    /**
     *
     * Creates the an ItemStack for this entity, with the correct model.
     *
     * @return The ItemStack
     */
    private ItemStack createDisplayedItem(){
        // Create base item
        ItemStack itemDisplayItem = new ItemStack(Material.DIAMOND_BLOCK);

        // Set model
        NamespacedKey modelKey = new NamespacedKey("midas",  modelName + "/" + this.UUID);
        ItemMeta meta = itemDisplayItem.getItemMeta();
        meta.setItemModel(modelKey);
        itemDisplayItem.setItemMeta(meta);

        return itemDisplayItem;
    }

    /**
     * Syncs the stored position of this bone and all of its child bones to the ItemDisplayGroup.
     * This doesn't actually move visually,
     * and should only be called inside the MIDAS plugin when spawning an ItemDisplayGroup.
     *
     * @param originPosition The originPosition (pivotPoint) of the itemDisplayGroup
     */
    public void syncPositionToDisplayGroup(Triple originPosition){
        for (Bone bone : childrenBones) {
            bone.syncPositionToDisplayGroup(originPosition);
        }
        if (!this.hasElement) {return;}

        position = new Triple(
                originPosition.x - (relPivot.x / 16),
                originPosition.y + (relPivot.y / 16),
                originPosition.z - (relPivot.z / 16)
        );
    }

    public void render(Triple originPosition, Player player){
        for(Bone bone : this.childrenBones){
            bone.render(originPosition, player);
        }
        if(this.hasElement && !this.renderingPlayers.contains(player)){
            Triple spawnPosition = new Triple(
                    originPosition.x - (relPivot.x / 16),
                    originPosition.y + (relPivot.y / 16),
                    originPosition.z - (relPivot.z / 16)
            );

            this.renderingPlayers.add(player);

            PacketSender.sendPacket(player, PacketCreator.addItemDisplayPacket(spawnPosition, entityID));
            ItemDisplayPacketData data =  new ItemDisplayPacketData();
            data.setDisplayedItem(this.displayedItem);
            //Bukkit.getScheduler().runTaskLater(MultiItemDisplayEngine.plugin, () -> {
                PacketSender.sendPacket(player, PacketCreator.setItemDisplayDataPacket(data, entityID));
            //}, 1);

            if (pivotPointDisplay == null){
                World world = player.getWorld();
                // Spawn pivot diamond BLOCK
                pivotPointDisplay = (ItemDisplay) world.spawnEntity(new Location(world, spawnPosition.x, spawnPosition.y, spawnPosition.z), EntityType.ITEM_DISPLAY);

                Transformation oldTransform = pivotPointDisplay.getTransformation();
                Transformation newTransform = new Transformation(
                        oldTransform.getTranslation(),
                        oldTransform.getLeftRotation(),
                        new Vector3f(0.05f, 0.05f, 0.05f),
                        oldTransform.getRightRotation()
                );
                pivotPointDisplay.setTransformation(newTransform);
                ItemStack diamondBlock;
                if (this.parentBone == null) {
                    diamondBlock = new ItemStack(Material.NETHERITE_BLOCK);
                }
                else {
                    diamondBlock = new ItemStack(Material.DIAMOND_BLOCK);
                }
                pivotPointDisplay.setItemStack(diamondBlock);
            }
        }
    }

    public void unrender(Player player){
        for(Bone bone : this.childrenBones){
            bone.unrender(player);
        }

        if(!this.hasElement || !this.renderingPlayers.contains(player)){
            return;
        }
        PacketSender.sendPacket(player,
                PacketCreator.removeItemDisplaysPacket(entityID));
        this.renderingPlayers.remove(player);
    }

    public ItemDisplay getItemDisplay() {
        return itemDisplay;
    }

    /**
     *
     * Reset the bones location relative to the defined ItemDisplay
     *
     * @param display the ItemDisplay to reset relative to
     */
    public void resetLocation(ItemDisplayGroup display){
        Location location = display.getPivotPoint();
        location.set(
                location.getX() - (relPivot.x / 16),
                location.getY() + (relPivot.y / 16),
                location.getZ() - (relPivot.z / 16)
        );

        Teleport.teleportSingleBone(this, location);
        Rotate.SetSingleBoneRotation(this, baseRotation);
    }

    public void resetRotation(){
       Rotate.SetBoneRotationWithChildren(this,  baseRotation);
    }

    /**
     * Resets the bones location relative to the defined ItemDisplay with children
     *
     * @param display the ItemDisplay to reset relative to
     * @param withChildren Should the children bones also have their rotation reset
     */
    public void resetLocation(ItemDisplayGroup display, boolean withChildren){
        resetLocation(display);
        if(withChildren){
            Location location = display.getPivotPoint();
            location.set(
                    location.getX() - (relPivot.x / 16),
                    location.getY() + (relPivot.y / 16),
                    location.getZ() - (relPivot.z / 16)
            );

            Teleport.teleportSingleBone(this, location);
            Rotate.SetSingleBoneRotation(this, baseRotation);

            for(Bone bone : this.childrenBones){
                bone.resetLocation(display, true);
            }
        }
    }

    public Triple getRelOrigin() {
        return relPivot.clone();
    }

    public void setRelOrigin(Triple relOrigin) {
        this.relPivot = relOrigin;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public int getEntityID() {return this.entityID;}

    public List<Bone> getChildrenBones() {
        return childrenBones;
    }

    /**
     *
     * Gets all the children bones of this bone and optionally a reference to itself.
     *
     * @param includeSelf A boolean where true means that the bone this method is called on will be included in the returned list, false means it isn't.
     * @return The list of bones either with or without this bone itself depending on the includeSelf parameter, or an empty list if there's no results.
     */
    public List<Bone> getAllChildrenBones(Boolean includeSelf) {
        List<Bone> childrenBones = new ArrayList<>();

        if(includeSelf) childrenBones.add(this);

        for(Bone child : this.childrenBones){
            childrenBones.addAll(child.getAllChildrenBones(includeSelf));
        }

        return childrenBones;
    }

    public boolean hasElement() {return this.hasElement;}

    public boolean hasChildren() {return !this.childrenBones.isEmpty();}

    public Triple getBaseRotation() {
        return baseRotation;
    }

    public void setBaseRotation(Triple baseRotation) {
        this.baseRotation = baseRotation;
    }

    public List<Player> getRenderingPlayers() {
        return renderingPlayers;
    }

    public Triple getPosition() {
        return position;
    }

    public void setPosition(Triple position) {
        this.position = position;
    }

    public Triple getCurrentRotation() {
        return currentRotation;
    }

    public void setChildrenBones(List<Bone> childrenBones) {
        this.childrenBones = childrenBones;
    }

    public void addChildrenBone(Bone childrenBone) {
        this.childrenBones.add(childrenBone);
    }

    public Bone getParentBone() {
        return parentBone;
    }

    public void setParentBone(Bone parentBone) {
        this.parentBone = parentBone;
    }

}
