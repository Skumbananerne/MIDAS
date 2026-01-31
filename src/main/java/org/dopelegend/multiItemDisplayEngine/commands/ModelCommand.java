package org.dopelegend.multiItemDisplayEngine.commands;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.TexturePack;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketCreator;
import org.dopelegend.multiItemDisplayEngine.packetHandler.PacketSender;
import org.dopelegend.multiItemDisplayEngine.utils.classes.EntityHandler;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ModelCommand {
    public static int spawnModelByNameCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red> <bold> Only players can execute this command");
            return 0;
        }

        EntityHandler entityHandler = EntityHandler.getEntityHandler(player.getUniqueId());
        int entityId = entityHandler.getID();

        PacketSender.sendPacket(player, PacketCreator.addItemDisplayPacket(player.getLocation(), entityId));
        PacketSender.sendPacket(player, PacketCreator.setItemDisplayDataPacket(entityId));



        if (ctx.getArgument("model name", String.class).equals("itemDisplayTest")){
            ItemDisplay itemDisplay = (ItemDisplay) player.getWorld().spawnEntity(new Location(player.getWorld(), 0.5, 100.5 ,0.5), EntityType.ITEM_DISPLAY);
            ItemStack diamondBlock = new ItemStack(Material.DIAMOND_BLOCK);
            itemDisplay.setItemStack(diamondBlock);
            new BukkitRunnable() {
                @Override
                public void run() {
                    testTeleport(itemDisplay);
                }
            }.runTaskTimer(MultiItemDisplayEngine.plugin, 0, 61);
            return 1;
        }

        

        ItemDisplayGroup itemDisplayGroup = new ItemDisplayGroup(new Location(player.getWorld(), 0.5, 100.5 ,0.5), ctx.getArgument("model name", String.class));
        itemDisplayGroup.spawn();

        itemDisplayGroup.playAnimation("animation");

        // Meget smuk rotation :D, vi skal måske lige finde ud af om vi vil gøre det på den måde jeg gør det (dele tingen op til mindre rotationer hver tick.).
        if (ctx.getArgument("model name", String.class).equals("jet")){
            new BukkitRunnable() {
                @Override
                public void run() {
                    itemDisplayGroup.teleportRelativeSmooth(new Triple(0, 0, -1), 5);
                    itemDisplayGroup.addRotationSmooth(new Triple(0, 0, -30), 5);
                }
            }.runTaskTimer(MultiItemDisplayEngine.plugin, 0L, 5L);
        }

        return 1;
    }

    public static void testTeleport(ItemDisplay itemDisplay) {
//        itemDisplay.setTeleportDuration(20);
//        itemDisplay.teleportAsync(itemDisplay.getLocation().clone().add(0, 2, 0));
//        Bukkit.getScheduler().runTaskLater(MultiItemDisplayEngine.plugin, () -> {
//            Location location = itemDisplay.getLocation().clone().add(0, -1, 0);
//            itemDisplay.setTeleportDuration(20);
//            itemDisplay.teleportAsync(location);
//        }, 20);
//        Bukkit.getScheduler().runTaskLater(MultiItemDisplayEngine.plugin, () -> {
//            itemDisplay.setTeleportDuration(20);
//            itemDisplay.teleportAsync(itemDisplay.getLocation().clone().add(0, 1, 0));
//        }, 40);
//        Bukkit.getScheduler().runTaskLater(MultiItemDisplayEngine.plugin, () -> {
//            itemDisplay.setTeleportDuration(0);
//            itemDisplay.teleportAsync(itemDisplay.getLocation().clone().add(0, -2, 0));
//        }, 60);

        itemDisplay.setTeleportDuration(30);
        Bukkit.getScheduler().runTaskLater(MultiItemDisplayEngine.plugin, () -> {
            itemDisplay.teleport(itemDisplay.getLocation().add(0, 1, 0));
        }, 1);
        Bukkit.getScheduler().runTaskLater(MultiItemDisplayEngine.plugin, () -> {
            itemDisplay.teleport(itemDisplay.getLocation().add(0, -1, 0));
        }, 30);
    }

    public static int generateTexturePack(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player)){
            ctx.getSource().getSender().sendRichMessage("<red> <bold> Only players can execute this command");
            return 0;
        }
        TexturePack.generateTexturePack();
        MultiItemDisplayEngine.packWebServer.reloadPackSnapshot();
        return 1;
    }

    public static CompletableFuture<Suggestions> suggestModels(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();

        for (File model : TexturePack.getAllFiles()) {
            String fileName = model.getName();
            fileName = fileName.split("\\.")[0];
            if (fileName.startsWith(remaining)) {
                builder.suggest(fileName);
            }
        }

        return builder.buildFuture();
    }
}
