package org.dopelegend.multiItemDisplayEngine.blockBench;

import com.google.gson.*;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
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
        return createBone(boneObject, null, modelData, modelFile.getName().substring(0, modelFile.getName().lastIndexOf('.')));
    }


    /**
     *
     * Gets a bone from a json bone object and the parent bone, use parent bone null for root bones. This function iterates over itself to create child bones.
     *
     * @param outlineObject The jsonObject of the outline
     * @param parent Parent bone
     * @return Created bone
     */
    static private Bone createBone(JsonObject outlineObject, Bone parent, JsonObject rootJson, String fileName){
        JsonArray originArray = outlineObject.getAsJsonArray("origin");
        JsonArray childrenArray = outlineObject.getAsJsonArray("children");

        List<String> uuids = new ArrayList<>();
        List<Element> elements = new ArrayList<>();
        for(int i = 0; i < childrenArray.size(); i++){
            Object object = childrenArray.get(i);
            if(!(object instanceof JsonObject)){
                uuids.add(childrenArray.get(i).getAsString());
            }
        }

        for(String uuid : uuids){
            elements.add(Element.getElementFromUuid(rootJson, uuid, fileName));
        }


        Element[] childrenElementArray = elements.toArray((new Element[0]));
        Bone bone = new Bone(
                new Triple(originArray.get(0).getAsDouble(), originArray.get(1).getAsDouble(),originArray.get(2).getAsDouble()),
                parent,
                new Bone[0],
                childrenElementArray,
                Uuid.getStringUuid());


        //Create children bones

        List<Bone> childrenBones = new ArrayList<Bone>();
        for(int i = 0; i < childrenArray.size(); i++){
            Object object = childrenArray.get(i);
            if(!(object instanceof JsonObject)){
                continue;
            }
            childrenBones.add(createBone(childrenArray.get(i).getAsJsonObject(), bone, rootJson, fileName));
        }
        Bone[] childrenBoneArray = childrenBones.toArray((new Bone[0]));
        bone.setChildrenBone(childrenBoneArray);

        return bone;
    }
}
