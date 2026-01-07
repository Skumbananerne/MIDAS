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
    private Triple rotation;

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
     * @param parentBone Parent of this bone
     * @param childrenBones All children direct child bones from this bone
     * @param UUID UUID of this bone
     */
    public Bone(Triple relPivot, Bone parentBone, List<Bone> childrenBones, String UUID, String modelName) {
        this.relPivot = relPivot;
        this.UUID = UUID;
        this.childrenBones = childrenBones;
        this.parentBone = parentBone;
        this.hasElement = true;
        this.modelName = modelName;
    }

    public void spawn(Triple originPosition, World world){
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

    public Triple getRotation() {
        return rotation;
    }

    public void setRotation(Triple rotation) {
        this.rotation = rotation;
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
