package org.dopelegend.multiItemDisplayEngine.blockBench;

import com.google.gson.*;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.TexturePack;
import org.dopelegend.multiItemDisplayEngine.files.utils.FileGetter;
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
     * @return The root bone
     */
    public static Bone getRootBone(File modelFile) {
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

        //Get rootBone
        JsonObject boneObject = modelData.getAsJsonArray("outliner").get(0).getAsJsonObject();
        JsonArray groupArray = modelData.get("groups").getAsJsonArray();
        return createBone(TexturePack.getBoneFromUUID(boneObject.get("uuid").getAsString(), groupArray), modelData, null);
    }


    /**
     *
     * Gets a bone from a json bone object and the parent bone, use parent bone null for root bones. This function iterates over itself to create child bones.
     *
     * @param modelData Json object of the model file
     * @param parent Parent bone
     * @return Created bone
     */
    static private Bone createBone(JsonObject boneObject, JsonObject modelData, Bone parent){
        // Get origin array
        JsonArray originArray = boneObject.getAsJsonArray("origin");

        // Get outlinerBone
        String boneUUID = boneObject.get("uuid").getAsString();

        JsonArray outlinerArray = modelData.getAsJsonArray("outliner");
        JsonObject outlinerBone = TexturePack.getOutlinerBoneFromUUID(boneUUID, outlinerArray);

        JsonArray childrenArray = outlinerBone.getAsJsonArray("children");

        //Finds the uuid of the first element in the children of the bone, or null if it has none
        String uuid = null;
        for(int i = 0; i < childrenArray.size(); i++){
            Object object = childrenArray.get(i);
            if(!(object instanceof JsonObject)){
                uuid = childrenArray.get(i).getAsString();
                break;
            }
        }

        Bone bone;
        if(uuid == null){
            // Bone does not have an element
            bone = new Bone(
                    new Triple(originArray.get(0).getAsDouble(), originArray.get(1).getAsDouble(),originArray.get(2).getAsDouble()),
                    parent,
                    new ArrayList<>(),
                    boneUUID
            );
        }
        else {
            // Bone have an element
            JsonArray elementPos = Objects.requireNonNull(getElement(modelData.get("elements").getAsJsonArray(), uuid)).getAsJsonArray("from");
            bone = new Bone(
                    new Triple(originArray.get(0).getAsDouble(), originArray.get(1).getAsDouble(),originArray.get(2).getAsDouble()),
                    new Triple(elementPos.get(0).getAsDouble(), elementPos.get(1).getAsDouble(), elementPos.get(2).getAsDouble()),
                    parent,
                    new ArrayList<>(),
                    boneUUID,
                    modelData.get("name").getAsString()
            );
        }


        //Create children bones
        for(int i = 0; i < childrenArray.size(); i++){
            Object object = childrenArray.get(i);
            if(!(object instanceof JsonObject)){
                continue;
            }
            JsonObject childOutlinerBone = childrenArray.get(i).getAsJsonObject();
            String childBoneUUID = childOutlinerBone.get("uuid").getAsString();
            JsonObject childBone = TexturePack.getBoneFromUUID(childBoneUUID, outlinerArray);
            if (childBone.isEmpty()) continue;
            bone.addChildrenBone(createBone(childBone, modelData, bone));
        }

        return bone;
    }

    /**
     *
     * @param elements The jsonArray of elements from the .bbmodel file.
     * @param uuid The uuid of the element to find
     * @return The element if it exists, or null if it doesn't
     */
    static private JsonObject getElement(JsonArray elements, String uuid){
        JsonObject element;
        String currentUUID;
        for(int i = 0; i < elements.size(); i++){
            currentUUID =  elements.get(i).getAsJsonObject().get("uuid").getAsString();
            if(currentUUID.equals(uuid)){
                element = elements.get(i).getAsJsonObject();
                return element;
            }
        }
        return null;
    }
}
