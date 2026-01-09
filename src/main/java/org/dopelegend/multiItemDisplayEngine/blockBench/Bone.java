package org.dopelegend.multiItemDisplayEngine.blockBench;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.movement.Teleport;
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

    private Triple relPivot;
    /**
     * This is the base rotation of this bone found from the .bbmodel file. This usually isn't what you want to change (look at currentRotation)
     */
    private Triple baseRotation;
    /**
     * This is the currentRotation of this bone as an euler angle in degrees, note that changing this won't actually change the rotation it will simply update the value.
     */
    private Triple currentRotation;

    private String UUID = "";
    private ItemDisplay itemDisplay;
    private List<Bone> childrenBones;
    private Bone parentBone;
    private String modelName;
    private

    boolean hasElement = false;

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
    }

    public void spawn(Triple originPosition, World world){
        for(int i = 0; i < this.childrenBones.size(); i++){
            this.childrenBones.get(i).spawn(originPosition, world);
        }
        if(this.hasElement && this.itemDisplay == null){
            Triple spawnPosition = new Triple(
                    originPosition.x - (relPivot.x / 16),
                    originPosition.y + (relPivot.y / 16),
                    originPosition.z - (relPivot.z / 16)
            );

            //spawn item display
            this.itemDisplay = (ItemDisplay) world.spawnEntity(new Location(world, spawnPosition.x, spawnPosition.y, spawnPosition.z), EntityType.ITEM_DISPLAY);

            ItemStack itemDisplayItem = new ItemStack(Material.DIAMOND_BLOCK);
            //Set item model
            NamespacedKey modelKey = new NamespacedKey("midas",  modelName + "/" + this.UUID);
            ItemMeta meta = itemDisplayItem.getItemMeta();
            meta.setItemModel(modelKey);
            itemDisplayItem.setItemMeta(meta);
            this.itemDisplay.setItemStack(itemDisplayItem);

            resetRotation();

            // Spawn pivot diamond BLOCK
            ItemDisplay pivotPointDisplay = (ItemDisplay) world.spawnEntity(new Location(world, spawnPosition.x, spawnPosition.y, spawnPosition.z), EntityType.ITEM_DISPLAY);
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
        return relPivot;
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

    public ItemDisplay getItemDisplay() {return this.itemDisplay;}

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
