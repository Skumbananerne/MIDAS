    package org.dopelegend.multiItemDisplayEngine;

    import org.bukkit.plugin.Plugin;
    import org.bukkit.plugin.java.JavaPlugin;
    import org.dopelegend.multiItemDisplayEngine.blockBench.generator.TexturePack;
    import org.dopelegend.multiItemDisplayEngine.commands.CommandListener;
    import org.dopelegend.multiItemDisplayEngine.files.generate.FileStructure;
    import org.dopelegend.multiItemDisplayEngine.texturePack.PackWebServer;

    import java.io.IOException;


    public final class MultiItemDisplayEngine extends JavaPlugin {

        public static Plugin plugin;
        public static PackWebServer packWebServer;

        @Override
        public void onEnable() {
            plugin = this;
            FileStructure.generateEntireFileStructure();
            new CommandListener(this);
            TexturePack.generateTexturePack();
            packWebServer = new PackWebServer();
            try {
                packWebServer.startHttpServer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            this.getServer().getPluginManager().registerEvents(new EventListener(), this);
        }

        @Override
        public void onDisable() {
            // Plugin shutdown logic
        }
    }
