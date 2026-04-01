package org.dopelegend.multiItemDisplayEngine.blockBench;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.utils.CustomModelData;
import org.dopelegend.multiItemDisplayEngine.utils.Uuid;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


/**
 * A custom class representing an element, when spawned this also includes an ItemDisplay.
 * This class tries to keep all values in the form they're found in the .bbmodel files.
 *
 */
public class Element {

    Triple from;
    Triple to;
    Triple origin;
    Triple rotation = new Triple(0, 0, 0);
    String uuid;
    String fileName;
    double[] resolution = new double[2];
    List<Pair<int[], String>> faces = new ArrayList<>();
    Pair<int[], String> faceNorth;
    Pair<int[], String> faceEast;
    Pair<int[], String> faceSouth;
    Pair<int[], String> faceWest;
    Pair<int[], String> faceUp;
    Pair<int[], String> faceDown;
    ItemDisplay itemDisplay;

    /**
     *
     * Creates an element from an element JsonObject from a .bbmodel file.
     *
     * @param elementJson The JsonObject representing one full element in a .bbmodel file.
     * @param rootJson The JsonObject representing the entire .bbmodel file.
     */
    public Element (JsonObject elementJson, JsonObject rootJson, String fileName){

        try {
            this.fileName = fileName;

            this.resolution[0] = rootJson.get("resolution").getAsJsonObject().get("width").getAsDouble();
            this.resolution[1] = rootJson.get("resolution").getAsJsonObject().get("height").getAsDouble();
            // Make uuid
            this.uuid = elementJson.get("uuid").getAsString();

            // Get from list
            JsonArray fromArray = elementJson.get("from").getAsJsonArray();
            // Make fromTriple
            this.from = new Triple(fromArray.get(0).getAsDouble(), fromArray.get(1).getAsDouble(), fromArray.get(2).getAsDouble());

            // Get to list
            JsonArray toArray = elementJson.get("to").getAsJsonArray();
            // Make toTriple
            this.to = new Triple(toArray.get(0).getAsDouble(), toArray.get(1).getAsDouble(), toArray.get(2).getAsDouble());

            // Get origin list
            JsonArray originArray = elementJson.get("origin").getAsJsonArray();
            // Make originTriple
            this.origin = new Triple(originArray.get(0).getAsDouble(), originArray.get(1).getAsDouble(), originArray.get(2).getAsDouble());

            // Check if element has rotation
            if (elementJson.has("rotation")){
                // Add rotation
                JsonArray rotationArray = elementJson.get("rotation").getAsJsonArray();
                this.rotation.setX(rotationArray.get(0).getAsDouble());
                this.rotation.setY(rotationArray.get(1).getAsDouble());
                this.rotation.setZ(rotationArray.get(2).getAsDouble());
            }

            // Make list of directions to loop through
            List<String> facestrings = new ArrayList<>();
            facestrings.add("north");
            facestrings.add("east");
            facestrings.add("south");
            facestrings.add("west");
            facestrings.add("up");
            facestrings.add("down");

            this.faces.add(faceNorth);
            this.faces.add(faceEast);
            this.faces.add(faceSouth);
            this.faces.add(faceWest);
            this.faces.add(faceUp);
            this.faces.add(faceDown);
            // Loop through faces
            JsonObject faceJson = elementJson.get("faces").getAsJsonObject();
            for (int i = 0; i < facestrings.size(); i++) {
                JsonArray uvJsonArray = faceJson.get(facestrings.get(i)).getAsJsonObject().get("uv").getAsJsonArray();
                int[] uvValues = new int[4];
                uvValues[0] = uvJsonArray.get(0).getAsInt();
                uvValues[1] = uvJsonArray.get(1).getAsInt();
                uvValues[2] = uvJsonArray.get(2).getAsInt();
                uvValues[3] = uvJsonArray.get(3).getAsInt();
                String textureId = "-1";
                if (faceJson.get(facestrings.get(i)).getAsJsonObject().has("texture")){
                    textureId = faceJson.get(facestrings.get(i)).getAsJsonObject().get("texture").getAsString();
                }

                faces.set(i, Pair.of(uvValues, textureId));
            }
        }
        catch (Exception e){
            MultiItemDisplayEngine.plugin.getLogger().severe("Something went wrong trying to initialize an element class");
            MultiItemDisplayEngine.plugin.getLogger().severe(e.getMessage());
            MultiItemDisplayEngine.plugin.getLogger().severe(Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     *
     * Copies an element from another element
     *
     * @param element The JsonObject representing the entire .bbmodel file.
     */
    public Element (Element element){
        this.from = element.getFrom();
        this.to = element.getTo();
        this.origin = element.getOrigin();
        this.rotation = element.getRotation();
        this.uuid = element.getUuid();
        this.fileName = element.getFileName();
        this.resolution = element.getResolution();
        this.faces = element.getFaces();
        this.faceNorth = element.getFaceNorth();
        this.faceEast = element.getFaceEast();
        this.faceSouth = element.getFaceSouth();
        this.faceWest = element.getFaceWest();
        this.faceUp = element.getFaceUp();
        this.faceDown = element.getFaceDown();
    }


    /**
     *
     * Spawns an Itemdisplay at the relative position of the element (originPosition+the from value of the element)
     *
     * @param originPosition The location of the ItemDisplayGroup this element is a part of.
     * @param world The world to spawn the element in
     */
    public void spawn(Triple originPosition, World world){
        // Make spawn location
        // 2) Corner you want to touch the block center
        double ax = Math.max(this.from.x, this.to.x); // max X
        double ay = Math.min(this.from.y, this.to.y); // min Y
        double az = Math.max(this.from.z, this.to.z); // max Z

        // 3) Map to world. If originPosition is the block CENTER, subtract 8
        double wx = originPosition.x + (ax - 8.0)/16.0;
        double wy = originPosition.y + (ay - 8.0)/16.0;
        double wz = originPosition.z + (az - 8.0)/16.0;

        Location spawnLoc = new Location(world, wx, wy, wz);
        // Spawn item Display
        this.itemDisplay = (ItemDisplay) world.spawnEntity(spawnLoc, EntityType.ITEM_DISPLAY);
        // Set rotation
        itemDisplay.setTransformation(new Transformation(
                new Vector3f(),
                new Quaternionf(),
                new Vector3f(1f, 1f, 1f),
                new Quaternionf().rotateXYZ((float)Math.toRadians(this.rotation.x), (float)Math.toRadians(this.rotation.y), (float)Math.toRadians(this.rotation.z))
        ));
        // Set model
        itemDisplay.setItemStack(CustomModelData.addCustomModelData(this.fileName+"_"+this.uuid, new ItemStack(Material.DIAMOND_BLOCK)));

    }


    /**
     *
     * Gets all elements in a .bbmodel file, in the form of a array of element classes
     *
     * @param file The root json of the .bbmodel file
     * @return The array of elements or an empty array if there's no elements
     */
    public static Element[] getAllElementFromFile(JsonObject file, String fileName){
        JsonArray jsonElements = file.get("elements").getAsJsonArray();
        Element[] elements = new Element[jsonElements.size()];
        for (int i = 0 ; i < jsonElements.size() ; i++){
            elements[i] = new Element(jsonElements.get(i).getAsJsonObject(), file, fileName);
        }
        return elements;
    }

    /**
     *
     * Finds element from a given uuid in a file
     *
     * @param rootJson The rootJson object of the entire .bbmodel file
     * @param uuid The uuid to look for. This is the Uuid in the .bbmodel file.
     * @return Returns an element with the uuid or null if it isn't in the file
     */
    public static Element getElementFromUuid(JsonObject rootJson, String uuid, String fileName){
        for (JsonElement element : rootJson.get("elements").getAsJsonArray()){
            if (Objects.equals(element.getAsJsonObject().get("uuid").getAsString(), uuid)){
                return new Element(element.getAsJsonObject(), rootJson, fileName);
            }
        }
        return null;
    }

    public Triple getFrom() {
        return from;
    }

    public Triple getTo() {
        return to;
    }

    public Triple getRotation() {
        return rotation;
    }

    public void setRotation(Triple rotation) {
        this.rotation = rotation;
    }

    public Triple getOrigin() {
        return origin;
    }

    public String getUuid() {
        return uuid;
    }

    public double[] getResolution() {
        return resolution;
    }

    public List<Pair<int[], String>> getFaces() {
        return faces;
    }

    public void setFaces(List<Pair<int[], String>> faces) {
        this.faces = faces;
    }

    public Pair<int[], String> getFaceNorth() {
        return faceNorth;
    }

    public void setFaceNorth(Pair<int[], String> faceNorth) {
        this.faceNorth = faceNorth;
    }

    public Pair<int[], String> getFaceEast() {
        return faceEast;
    }

    public void setFaceEast(Pair<int[], String> faceEast) {
        this.faceEast = faceEast;
    }

    public Pair<int[], String> getFaceSouth() {
        return faceSouth;
    }

    public void setFaceSouth(Pair<int[], String> faceSouth) {
        this.faceSouth = faceSouth;
    }

    public Pair<int[], String> getFaceWest() {
        return faceWest;
    }

    public void setFaceWest(Pair<int[], String> faceWest) {
        this.faceWest = faceWest;
    }

    public Pair<int[], String> getFaceUp() {
        return faceUp;
    }

    public void setFaceUp(Pair<int[], String> faceUp) {
        this.faceUp = faceUp;
    }

    public Pair<int[], String> getFaceDown() {
        return faceDown;
    }

    public void setFaceDown(Pair<int[], String> faceDown) {
        this.faceDown = faceDown;
    }

    public String getFileName(){
        return this.fileName;
    }

}
