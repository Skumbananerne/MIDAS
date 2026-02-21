    package org.dopelegend.multiItemDisplayEngine;

    import net.minecraft.network.protocol.Packet;
    import org.bukkit.Material;
    import org.bukkit.configuration.file.FileConfiguration;
    import org.bukkit.craftbukkit.entity.CraftEntity;
    import org.bukkit.entity.EntityType;
    import org.bukkit.inventory.ItemFlag;
    import org.bukkit.inventory.ItemStack;
    import org.bukkit.inventory.meta.ItemMeta;
    import org.bukkit.persistence.PersistentDataType;
    import org.bukkit.plugin.Plugin;
    import org.bukkit.plugin.java.JavaPlugin;
    import org.bukkit.scheduler.BukkitRunnable;
    import org.dopelegend.multiItemDisplayEngine.blockBench.generator.TexturePack;
    import org.dopelegend.multiItemDisplayEngine.commands.CommandListener;
    import org.dopelegend.multiItemDisplayEngine.commands.ModelCommand;
    import org.dopelegend.multiItemDisplayEngine.files.generate.FileStructure;
    import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.PacketUpdater;
    import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketCreator;
    import org.dopelegend.multiItemDisplayEngine.texturePack.PackWebServer;
    import org.dopelegend.multiItemDisplayEngine.utils.Timer;

    import java.io.IOException;
    import java.util.function.Consumer;


    public final class MultiItemDisplayEngine extends JavaPlugin {

        public static Plugin plugin;
        public static PackWebServer packWebServer;
        public static FileConfiguration config;

        @Override
        public void onEnable() {
            plugin = this;

            saveDefaultConfig();
            config = getConfig();

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

            PacketUpdater.getInstance();

            ItemStack itemStack = new ItemStack(Material.COPPER_SWORD);
            itemStack.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }

        @Override
        public void onDisable() {
            // Plugin shutdown logic
        }
    }
