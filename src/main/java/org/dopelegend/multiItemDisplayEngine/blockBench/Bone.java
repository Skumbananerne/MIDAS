package org.dopelegend.multiItemDisplayEngine.blockBench;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.utils.CustomModelData;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.util.ArrayList;
import java.util.List;


/**
 *
 * A single bone of a model, these are the smallest parts that models are split into each bone is equal to either one or zero item displays.
 * Each bone has a parent bone (except the root bone), as well as children bones a bone is not guaranteed to have children bones.
 *
 */
public class Bone {

    Triple relPivot;
    Triple offset;

    private String UUID = "";
    private ItemDisplay itemDisplay;
    private List<Bone> childrenBones;;
    private Bone parentBone;
    private String modelName;

    boolean hasElement;

    /**
     *
     * When this constructor is used we assume the bone doesn't have an element.
     *
     * @param relPivot The origin represented by 3 doubles in the form of a triple.
     * @param parentBone Parent of this bone
     * @param childrenBones All children direct child bones from this bone
     * @param UUID UUID of this bone
     */
    public Bone(Triple relPivot, Bone parentBone, List<Bone> childrenBones, String UUID) {
        this.relPivot = relPivot;
        this.UUID = UUID;
        this.childrenBones = childrenBones;
        this.parentBone = parentBone;
        this.hasElement = false;
    }

    /**
     *
     * When this constructor is used we assume the bone have an element.
     *
     * @param relPivot The origin represented by 3 doubles in the form of a triple.
     * @param offset The origin represented by 3 doubles in the form of a triple.
     * @param parentBone Parent of this bone
     * @param childrenBones All children direct child bones from this bone
     * @param UUID UUID of this bone
     */
    public Bone(Triple relPivot, Triple offset, Bone parentBone, List<Bone> childrenBones, String UUID, String modelName) {
        this.relPivot = relPivot;
        this.UUID = UUID;
        this.childrenBones = childrenBones;
        this.parentBone = parentBone;
        this.hasElement = true;
        this.offset = offset;
        this.modelName = modelName;
    }

    public void spawn(Triple originPosition, World world){
        if(this.hasElement && this.itemDisplay == null){
            Triple spawnPosition = new Triple(
                    originPosition.x - (this.offset.x / 16),
                    originPosition.y + (this.offset.y / 16),
                    originPosition.z - (this.offset.z / 16)
            );

            //Spawn item display
            this.itemDisplay = (ItemDisplay) world.spawnEntity(new Location(world, spawnPosition.x, spawnPosition.y, spawnPosition.z), EntityType.ITEM_DISPLAY);

            ItemStack itemDisplayItem = new ItemStack(Material.DIAMOND_BLOCK);
            //Set Custom Model Data
            itemDisplayItem = CustomModelData.addCustomModelData("midas:" + modelName + "/" + this.UUID, itemDisplayItem);
            this.itemDisplay.setItemStack(itemDisplayItem);
        }
        for(int i = 0; i < this.childrenBones.size(); i++){
            this.childrenBones.get(i).spawn(originPosition, world);
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

    public List<Bone> getChildrenBones() {
        return childrenBones;
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
