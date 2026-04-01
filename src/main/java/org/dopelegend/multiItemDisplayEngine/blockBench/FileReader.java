package org.dopelegend.multiItemDisplayEngine.blockBench;

import com.google.gson.*;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.TexturePack;
import org.dopelegend.multiItemDisplayEngine.files.utils.FileGetter;
import org.dopelegend.multiItemDisplayEngine.utils.Uuid;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FileReader {

    /**
     *
     * Gets a model file with the path from the model folder
     *
     * @param path Path to model file
     * @return found model file or null if none was found.
     */
    public static File getModelFile(String path) {
        File file = new File(FileGetter.getModelFolder(), path + ".bbmodel");
        if(!file.exists()){
            MultiItemDisplayEngine.plugin.getLogger().warning("The model file could not be found: " + path + ".bbmodel");
            return null;
        }
        return file;
    }

    /**
     *
     * Gets the root bone from a model file, this needs to be a .bbmodel file, and won't work probably if the model has multiple root bones.
     *
     * @param modelFile The file to get the root bone from
     * @return The root bone or null if it couldn't be acquired
     */
    public static Bone getRootBone(File modelFile) {

        JsonObject modelData = getRootJsonObject(modelFile);
        if(modelData == null) return null;

        //Get rootBone
        JsonObject boneObject = modelData.getAsJsonArray("outliner").get(0).getAsJsonObject();
        JsonArray groupArray = modelData.get("groups").getAsJsonArray();
        return createBone(TexturePack.getBoneFromUUID(boneObject.get("uuid").getAsString(), groupArray), modelData, null);
    }

    /**
     *
     * Gets the root JsonObject in a certain file. This file needs to be a .bbmodel file
     *
     * @param modelFile The file to get the root JsonObject from
     * @return The root JsonObject or null if it couldn't be acquired for one reason or another
     */
    public static JsonObject getRootJsonObject(File modelFile) {
        //File doesn't exist
        if(!modelFile.exists()){
            MultiItemDisplayEngine.plugin.getLogger().warning("The model file could not be found: " + modelFile.getPath());
            return null;
        }

        //File isn't a file (folder)
        if(!modelFile.isFile()){
            MultiItemDisplayEngine.plugin.getLogger().warning("The provided modelFile is a folder. should be a file: " + modelFile.getName());
            return null;
        }
        //File isn't readable
        if(!modelFile.canRead()){
            MultiItemDisplayEngine.plugin.getLogger().warning("The model file can't be read: " + modelFile.getName());
            return null;
        }
        //File isn't a .bbmodel file
        if(!modelFile.getName().endsWith(".bbmodel")){
            MultiItemDisplayEngine.plugin.getLogger().warning("The model file is not a file type .bbmodel: " + modelFile.getName());
            return null;
        }
        //Declare rootJsonObject
        JsonObject modelData;
        Gson gson = new Gson();

        //Get rootJsonObject
        try(java.io.FileReader reader = new java.io.FileReader(modelFile)){
            JsonElement root = gson.fromJson(reader, JsonElement.class);
            modelData = root.getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return modelData;
    }

    /**
     *
     * Gets a bone from a json bone object and the parent bone, use parent bone null for root bones. This function iterates over itself to create child bones.
     *
     * @param outlineObject The jsonObject of the outline
     * @param parent Parent bone
     * @return Created bone
     */
    static private Bone createBone(JsonObject boneObject, JsonObject modelData, Bone parent){
        // Get origin array
        JsonArray originArray = boneObject.getAsJsonArray("origin");

        JsonArray rotationArray = boneObject.getAsJsonArray("rotation");
        Triple rotation = new Triple(0, 0, 0);
        if(rotationArray != null){
            rotation = new Triple(rotationArray.get(0).getAsDouble(), rotationArray.get(1).getAsDouble(), rotationArray.get(2).getAsDouble());
        }

        // Get outlinerBone
        String boneUUID = boneObject.get("uuid").getAsString();

        JsonArray outlinerArray = modelData.getAsJsonArray("outliner");
        JsonObject outlinerBone = TexturePack.getOutlinerBoneFromUUID(boneUUID, outlinerArray);

        JsonArray groupArray = modelData.getAsJsonArray("groups");

        JsonArray childrenArray = outlinerBone.getAsJsonArray("children");

        //Finds the uuid of the first element in the children of the bone, or null if it has none
        String uuid = null;
        for(Object object : childrenArray){
            if(object instanceof JsonPrimitive jsonPrimitive) {
                if (!jsonPrimitive.isString()) continue;
                uuid = jsonPrimitive.getAsString();
                break;
            }
        }

        Bone bone;
        if(uuid == null){
            bone = new Bone(
                    new Triple(originArray.get(0).getAsDouble(), originArray.get(1).getAsDouble(),originArray.get(2).getAsDouble()),
                    rotation,
                    parent,
                    new ArrayList<>(),
                    boneUUID
            );
        }
        else {
            // Bone has an element
            bone = new Bone(
                    new Triple(originArray.get(0).getAsDouble(), originArray.get(1).getAsDouble(),originArray.get(2).getAsDouble()),
                    rotation,
                    parent,
                    new ArrayList<>(),
                    boneUUID,
                    modelData.get("name").getAsString()
            );
        }


        //Loop through outliner bones
        for(JsonElement element : childrenArray){
            if(!(element instanceof JsonObject childOutlinerBone)) continue;

            // Get child bone in 'groups'
            String childBoneUUID = childOutlinerBone.get("uuid").getAsString();
            JsonObject childBone = TexturePack.getBoneFromUUID(childBoneUUID, groupArray);
            if (childBone.isEmpty()) continue;

            // Add child bone as a child
            bone.addChildrenBone(createBone(childBone, modelData, bone));
        }
        Bone[] childrenBoneArray = childrenBones.toArray((new Bone[0]));
        bone.setChildrenBone(childrenBoneArray);

        return bone;
    }
}
