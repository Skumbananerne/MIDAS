package org.dopelegend.multiItemDisplayEngine.texturePack;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.files.utils.FileGetter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;

public class PackWebServer extends JavaPlugin {
    private HttpServer httpServer;
    private Path packZipPath = FileGetter.getTexturePackFolder().toPath().resolve("pack.zip");

    private int defPort = 25566;
    private final String packFileName = "pack.zip";
    private String publicHost;

    private final UUID packUuid = UUID.fromString("11111111-2222-3333-4444-555555555555");

    public void startHttpServer() throws IOException {
        if(MultiItemDisplayEngine.config.getString("overrides.pack.host-type", "local").equalsIgnoreCase("local")){
            httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", MultiItemDisplayEngine.config.getInt("overrides.pack.port", defPort)), 0);

            httpServer.createContext("/" + packFileName, this::handlePackRequest);

            httpServer.setExecutor(null);
            httpServer.start();
        }

        URL url = new URL("https://api.ipify.org");
        BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        publicHost = in.readLine();
        in.close();

        refreshTexturePack();
    }

    private void handlePackRequest(HttpExchange ex) throws IOException {
        if (!Files.exists(packZipPath)) {
            ex.sendResponseHeaders(404, -1);
            ex.close();
            return;
        }

        byte[] bytes = Files.readAllBytes(packZipPath);

        Headers h = ex.getResponseHeaders();
        h.set("Content-Type", "application/zip");
        h.set("Content-Disposition", "attachment; filename=\"" + packFileName + "\"");
        h.set("Cache-Control", "no-cache, no-store, must-revalidate");

        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    public boolean refreshTexturePack(){
        ResourcePackInfo info;
        try {
            String sha1Hex = sha1Hex(packZipPath);
            URI uri = URI.create("http://" + MultiItemDisplayEngine.config.getString("overrides.pack.host", publicHost) + ":" + MultiItemDisplayEngine.config.getInt("overrides.pack.port", defPort) + "/" + packFileName);

            info = ResourcePackInfo.resourcePackInfo(packUuid, uri, sha1Hex);
        } catch (Exception e) {
            MultiItemDisplayEngine.plugin.getLogger().warning("Failed to give player pack.");
            MultiItemDisplayEngine.plugin.getLogger().warning(e.getMessage());
            return false;
        }

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(info)
                .prompt(Component.text("This server uses a resource pack. Please accept to play."))
                .required(true)
                .build();

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.sendResourcePacks(request);
        }

        return true;
    }

    private static String sha1Hex(Path file) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] data = Files.readAllBytes(file);
        byte[] digest = sha1.digest(data);
        return HexFormat.of().formatHex(digest);
    }

    public boolean givePlayerPack(Player player){
        ResourcePackInfo info;
        try {
            String sha1Hex = sha1Hex(packZipPath);
            URI uri = URI.create("http://" + MultiItemDisplayEngine.config.getString("overrides.pack.host", publicHost) + ":" + MultiItemDisplayEngine.config.getInt("overrides.pack.port", defPort) + "/" + packFileName);
            info = ResourcePackInfo.resourcePackInfo(packUuid, uri, sha1Hex);
        } catch (Exception e) {
            MultiItemDisplayEngine.plugin.getLogger().warning("Failed to give player pack.");
            MultiItemDisplayEngine.plugin.getLogger().warning(e.getMessage());
            return false;
        }

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(info)
                .prompt(Component.text("This server uses a resource pack. Please accept to play."))
                .required(true)
                .build();
        player.sendResourcePacks(request);

        return true;
    }
}
