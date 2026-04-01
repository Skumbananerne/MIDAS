package org.dopelegend.multiItemDisplayEngine.texturePack;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.files.utils.FileGetter;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public final class PackWebServer {

    private static final String PACK_FILE_NAME = "pack.zip";
    private static final UUID PACK_UUID = UUID.fromString("2c6f8b1d-45a8-4c8e-9b2e-4f5f7e2a1c11");

    private final Path packZipPath = FileGetter.getTexturePackFolder().toPath().resolve(PACK_FILE_NAME);

    private HttpServer httpServer;
    private ExecutorService executor;

    // snapshot of the current serving pack
    private final AtomicReference<PackSnapshot> snapshotRef = new AtomicReference<>(PackSnapshot.empty());

    public void startHttpServer() throws IOException {
        int port = MultiItemDisplayEngine.config.getInt("overrides.pack.port", 25566);

        // get pack info (sha1)
        reloadPackSnapshot();

        httpServer = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        httpServer.createContext("/" + PACK_FILE_NAME, this::handlePackRequest);

        // thread pool to stpo 1 client from blocking
        executor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        httpServer.setExecutor(executor);

        httpServer.start();

        MultiItemDisplayEngine.plugin.getLogger().info("[PackWebServer] Serving /" + PACK_FILE_NAME + " on port " + port);
    }

    public void stopHttpServer() {
        if (httpServer != null) {
            httpServer.stop(0);
            httpServer = null;
        }
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    /**
     * Call this after you rebuild/replace pack.zip.
     * It swaps the in-memory bytes + sha1 atomically.
     */
    public void reloadPackSnapshot() {
        try {
            if (!Files.exists(packZipPath)) {
                snapshotRef.set(PackSnapshot.empty());
                MultiItemDisplayEngine.plugin.getLogger().warning("[PackWebServer] pack.zip not found at: " + packZipPath);
                return;
            }

            // compute and save sha1
            byte[] bytes = Files.readAllBytes(packZipPath);
            String sha1 = sha1Hex(bytes);

            snapshotRef.set(new PackSnapshot(bytes, sha1));

            for (Player player : Bukkit.getOnlinePlayers()) {
                givePlayerPack(player);
            }
        } catch (Exception e) {
            snapshotRef.set(PackSnapshot.empty());
        }
    }

    private void handlePackRequest(HttpExchange ex) throws IOException {
        try {
            PackSnapshot snap = snapshotRef.get();
            if (!snap.isReady()) {
                ex.sendResponseHeaders(404, -1);
                return;
            }

            String method = ex.getRequestMethod();
            if (!method.equalsIgnoreCase("GET") && !method.equalsIgnoreCase("HEAD")) {
                ex.sendResponseHeaders(405, -1);
                return;
            }

            Headers h = ex.getResponseHeaders();
            h.set("Content-Type", "application/zip");
            h.set("Content-Disposition", "attachment; filename=\"" + PACK_FILE_NAME + "\"");
            h.set("Cache-Control", "no-store");
            h.set("Content-Length", String.valueOf(snap.bytes.length));

            if (method.equalsIgnoreCase("HEAD")) {
                ex.sendResponseHeaders(200, -1);
                return;
            }

            ex.sendResponseHeaders(200, snap.bytes.length);
            try (OutputStream os = ex.getResponseBody()) {
                os.write(snap.bytes);
                os.flush();
            }
        } finally {
            ex.close();
        }
    }

    public void givePlayerPack(Player player) {
        PackSnapshot snap = snapshotRef.get();
        if (!snap.isReady()) {
            MultiItemDisplayEngine.plugin.getLogger().warning("[PackWebServer] Can't send pack: snapshot not ready.");
            return;
        }

        String host = MultiItemDisplayEngine.config.getString("overrides.pack.host", "127.0.0.1");
        int port = MultiItemDisplayEngine.config.getInt("overrides.pack.port", 25566);

        URI uri = URI.create("http://" + host + ":" + port + "/" + PACK_FILE_NAME);

        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(PACK_UUID, uri, snap.sha1);

        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(info)
                .prompt(Component.text("This server uses a resource pack. Please accept to play."))
                .required(true)
                .replace(true)
                .build();

        player.sendResourcePacks(request);
    }

    private static String sha1Hex(byte[] data) throws Exception {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] digest = sha1.digest(data);
        return HexFormat.of().formatHex(digest);
    }

    private static final class PackSnapshot {
        final byte[] bytes;
        final String sha1;

        private PackSnapshot(byte[] bytes, String sha1) {
            this.bytes = bytes;
            this.sha1 = sha1;
        }

        static PackSnapshot empty() {
            return new PackSnapshot(new byte[0], "");
        }

        boolean isReady() {
            return bytes != null && bytes.length > 0 && sha1 != null && !sha1.isEmpty();
        }
    }
}
