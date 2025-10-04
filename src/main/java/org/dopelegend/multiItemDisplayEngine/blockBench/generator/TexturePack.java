package org.dopelegend.multiItemDisplayEngine.blockBench.generator;

import com.google.gson.*;
import it.unimi.dsi.fastutil.Pair;
import org.bukkit.Bukkit;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.Element;
import org.dopelegend.multiItemDisplayEngine.files.utils.FileGetter;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.utils.Uuid;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class TexturePack {
    /**
     *
     * Gets all model files in the model folder
     *
     * @return Array of all model files in the model folder and null if no files were found.
     */
    public static File[] getAllFiles() {
        File dir = FileGetter.getModelFolder();
        FilenameFilter filter = (file, name) -> name.toLowerCase().endsWith("bbmodel");

        return dir.listFiles(filter);
    }


    /**
     *
     * Generates a texturepack in the temp folder, from all the .bbmodel files in the models folder.
     *
     * @return Whether the generation was successful or not
     */
    public static boolean generateTexturePack() {
        File[] files = getAllFiles();
        if (files==null) return true;

        JsonObject[] modelFilesJson = new JsonObject[files.length];
        String[] modelFilesName = new String[files.length];
        Gson gson = new Gson();

        ItemDisplayGroup.resetRegisteredItemDisplayGroup();
        for (int i = 0; i < files.length; i++){
            try(FileReader reader = new FileReader(files[i])){
                JsonElement root = gson.fromJson(reader, JsonElement.class);
                modelFilesJson[i] = root.getAsJsonObject();
                modelFilesName[i] = files[i].getName().substring(0, files[i].getName().lastIndexOf('.'));

                ItemDisplayGroup itemDisplayGroup = new ItemDisplayGroup(files[i].getName().substring(0, files[i].getName().lastIndexOf('.')));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        // Change this to change the texture pack name
        String texturePackName = "TexturePack";
        File workingDir = new File(FileGetter.getTempFolder(), texturePackName);
        deleteTexturepackFolder(workingDir);

        workingDir.mkdirs();

        try{
            Element[] elements = getAllElements(modelFilesJson, modelFilesName);
            // Generate file structure
            generatePackMeta(workingDir);
            generateModelOverrider(workingDir, elements);
            generateTextures(workingDir, modelFilesJson);
            generateModels(workingDir, elements);
        } catch (Exception e) {
            deleteTexturepackFolder(workingDir);
            MultiItemDisplayEngine.plugin.getLogger().warning(e.getMessage());
            MultiItemDisplayEngine.plugin.getLogger().warning(Arrays.toString(e.getStackTrace()));
            return false;
        }

        //DEV
        try {
            Path appData = Paths.get(System.getenv("APPDATA"));
            Path resourcePacks = appData.resolve(".minecraft").resolve("resourcepacks");
            Path targetPack = resourcePacks.resolve(texturePackName);

            // Delete folder recursively if it exists
            if (Files.exists(targetPack)) {
                Files.walk(targetPack)
                        .sorted((a, b) -> b.compareTo(a)) // delete children before parent
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException e) {
                                MultiItemDisplayEngine.plugin.getLogger().warning(Arrays.toString(e.getStackTrace()));
                            }
                        });
            }

            Files.move(workingDir.toPath(), resourcePacks.resolve(workingDir.getName()));
        } catch (Exception e){
            MultiItemDisplayEngine.plugin.getLogger().warning(e.getMessage());
            MultiItemDisplayEngine.plugin.getLogger().warning(Arrays.toString(e.getStackTrace()));
            return  false;
        }

        //deleteTexturepackFolder(workingDir);
        return true;
    }


    /**
     *
     * generates the pack.mcmeta file in the root texturepack file (workingDir/pack.mcmeta)
     *
     * @param workingDir The root texturepack file
     */
    private static void generatePackMeta(File workingDir){
        // Generate pack.mcmeta
        try {
            File packMeta = new File(workingDir, "pack.mcmeta");
            Files.writeString(packMeta.toPath(),
                    """
        {
          "pack": {
            "pack_format": 64,
            "description": "Autogenerated by MultiItemDisplayEngine using Blockbench models"
          }
        }
        """);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }


    /**
     *
     * Generates the model overrider file in the texture pack (workingDir/assets/minecraft/items/diamond.json), the workingDir is the root texturepack file.
     *
     * @param workingDir The root texturepack file
     */
    private static void generateModelOverrider(File workingDir, Element[] elements){
        // Generate pack.mcmeta
        try {
            JsonArray cases = new JsonArray();

            for (Element element : elements){
                String uuid = element.getFileName() + "_" + element.getUuid();

                JsonObject boneEntry = new JsonObject();
                boneEntry.addProperty("when", uuid);

                JsonObject model = new JsonObject();
                model.addProperty("type", "model");
                model.addProperty("model", "item/" + uuid);
                boneEntry.add("model", model);

                cases.add(boneEntry);
            }

            // Fallback object
            JsonObject fallback = new JsonObject();
            fallback.addProperty("type", "model");
            fallback.addProperty("model", "block/diamond_block");

            // Model object
            JsonObject model = new JsonObject();
            model.addProperty("type", "select");
            model.addProperty("property", "custom_model_data");
            model.add("cases", cases);
            model.add("fallback", fallback);

            // Wrap it in root
            JsonObject root = new JsonObject();
            root.add("model", model);

            // Pretty-print
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String json = gson.toJson(root);

            // Write to file
            File file = new File(workingDir, "assets/minecraft/items/diamond_block.json");
            file.getParentFile().mkdirs();
            Files.writeString(file.toPath(), json);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * Generates the models folder (workingDir/assets/minecraft/models) and its subdirectories (models/item/uuid.json).
     * The main point of this function is creating all the modelname.json files from the .bbmodel files in the plugin's models folder
     *
     * @param workingDir The root texturepack file
     */
    private static void generateModels(File workingDir, Element[] elements){
        File itemModelsFolder = new File(workingDir, "assets/minecraft/models/item");
        if (!itemModelsFolder.exists()) {
            itemModelsFolder.mkdirs();
        }

        for (Element element : elements){
            String uuid = element.getUuid();

            JsonObject rootJson = new JsonObject();
            rootJson.addProperty("format_version", "1.21.8");
            rootJson.addProperty("credit", "Autogenerated by MultiItemDisplayEngine using Blockbench models");

            // TEXTURE SIZE
            JsonArray resolution = new JsonArray();
            resolution.add(element.getResolution()[0]);
            resolution.add(element.getResolution()[1]);

            rootJson.add("texture_size", resolution);
            // ---------

            // TEXTURE
            JsonObject texturesObject = new JsonObject();
            texturesObject.addProperty("-1", "block/diamond_block");

            // known ids so we don't duplicate entries
            List<String> knownIds = new ArrayList<>();
            String[] directions = {
                    "north",
                    "east",
                    "south",
                    "west",
                    "up",
                    "down",
            };

            List<Pair<int[], String>> faces = element.getFaces();

            for(Pair<int[], String> face : faces){
                String id = face.right();

                if(knownIds.contains(id)){
                   continue;
                }
                knownIds.add(id);
                texturesObject.addProperty(id, "item/" + element.getFileName() + "_" + id);
            }
            rootJson.add("textures", texturesObject);
            // ---------

            // ELEMENT ARRAY
            JsonArray rootElementObj = new JsonArray();
            JsonObject elementObj = new JsonObject();

            Triple from = element.getFrom();

            JsonArray fromArray = new JsonArray();
            fromArray.add(0);
            fromArray.add(0);
            fromArray.add(0);
            elementObj.add("from", fromArray);

            Triple to = element.getTo();

            JsonArray toArray = new JsonArray();
            toArray.add(to.x-from.x);
            toArray.add(to.y-from.y);
            toArray.add(to.z-from.z);
            elementObj.add("to", toArray);

            // faces
            JsonObject facesObj = new JsonObject();

            for(int i = 0; i < directions.length; i++){
                JsonObject faceObj = new JsonObject();
                JsonArray uvObj = new JsonArray();

                uvObj.add(faces.get(i).left()[0] / (element.getResolution()[0] / 16));
                uvObj.add(faces.get(i).left()[1] / (element.getResolution()[1] / 16));
                uvObj.add(faces.get(i).left()[2] / (element.getResolution()[0] / 16));
                uvObj.add(faces.get(i).left()[3] / (element.getResolution()[1] / 16));

                faceObj.add("uv", uvObj);
                faceObj.addProperty("texture", "#" + faces.get(i).right());

                facesObj.add(directions[i], faceObj);
            }

            elementObj.add("faces", facesObj);
            rootElementObj.add(elementObj);
            rootJson.add("elements", rootElementObj);
            // ---------


            try {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                String json = gson.toJson(rootJson);

                File file = new File(workingDir, "assets/minecraft/models/item/" + element.getFileName() + "_" + uuid + ".json");
                file.getParentFile().mkdirs();
                Files.writeString(file.toPath(), json);
            } catch (IOException e) {
                MultiItemDisplayEngine.plugin.getLogger().warning(e.getMessage());
                MultiItemDisplayEngine.plugin.getLogger().warning(Arrays.toString(e.getStackTrace()));
            }
        }
    }


    /**
     *
     * Generates the textures folder (workingDir/assets/minecraft/textures) and its subdirectories (textures/item/uuid.).
     * The main point of this function is creating all the texture files from the .bbmodel files in the plugin's models folder
     *
     * @param workingDir The root texturepack file
     */
    private static void generateTextures(File workingDir, JsonObject[] files){
        //generate path
        File textureFolder = new File(workingDir, "assets/minecraft/textures/item");
        textureFolder.mkdirs();
        for (JsonObject modelFile : files) {

            JsonArray textureArray = modelFile.get("textures").getAsJsonArray();


            for (int i = 0; i < textureArray.size(); i++) {
                JsonObject texture = textureArray.get(i).getAsJsonObject();

                File file = new File(
                        workingDir,
                        "assets/minecraft/textures/item/" + modelFile.get("name").getAsString() + "_" + texture.get("id").getAsString() + ".png"
                );

                String source = texture.get("source").getAsString();
                String base64Data = source.replaceFirst("^data:image/[^;]+;base64,", "");

                byte[] imageBytes = Base64.getDecoder().decode(base64Data);

                try (OutputStream out = new FileOutputStream(file)) {
                    out.write(imageBytes);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


    private static Element[] getAllElements(JsonObject[] modelFilesJson, String[] fileName){
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < modelFilesJson.length; i++) {
            Element[] el = Element.getAllElementFromFile(modelFilesJson[i], fileName[i]);
            elements.addAll(Arrays.asList(el));
        }
        return elements.toArray(new Element[0]);
    }


    private static boolean deleteTexturepackFolder(File dir){
        try {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (!deleteTexturepackFolder(f)) {
                            return false;
                        }
                    }
                }
            }
            return dir.delete();
        } catch (Exception e){
            if (dir != null && dir.exists()) {
                MultiItemDisplayEngine.plugin.getLogger().severe("Could not delete texturepack folder\n" + e.getMessage());
            }

            return false;
        }

    }

}

