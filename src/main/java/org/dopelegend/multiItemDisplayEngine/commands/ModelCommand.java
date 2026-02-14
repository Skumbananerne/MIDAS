package org.dopelegend.multiItemDisplayEngine.commands;


import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.TexturePack;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.movement.TeleportSmooth;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ModelCommand {
    public static int spawnModelByNameCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red> <bold> Only players can execute this command");
            return 0;
        }

        ItemDisplayGroup itemDisplayGroup = new ItemDisplayGroup(new Location(player.getWorld(), 0.5, 1.5 ,0.5), ctx.getArgument("model name", String.class));
        itemDisplayGroup.spawn();

        Location teleportLoc = new Location(player.getWorld(), 0.5, 1.5 ,0.5);
        //itemDisplayGroup.playAnimation("animation");
        itemDisplayGroup.setPivotPoint(teleportLoc.clone().add(0, 1, 0));

        new BukkitRunnable() {
            @Override
            public void run() {
                TeleportSmooth.TeleportSingleBoneSmooth(itemDisplayGroup.getRootBone(), teleportLoc.add(0, 1, 0), 20);
            }
        }.runTaskLater(MultiItemDisplayEngine.plugin, 1L);

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
            ctx.getSource().getSender().sendRichMessage("<red><bold>Only players can execute this command");
            return 0;
        }
        TexturePack.generateTexturePack();
        MultiItemDisplayEngine.packWebServer.reloadPackSnapshot();
        return 1;
    }

    public static int deleteItemDisplayGroup(CommandContext<CommandSourceStack> ctx, boolean single) {
        if(single){
            UUID uuid = UUID.fromString(ctx.getArgument("group uuid", String.class));
            ItemDisplayGroup group = ItemDisplayGroup.getItemDisplayGroup(uuid);

            if(group != null){
                group.destroy();
                return 0;
            }
            ctx.getSource().getSender().sendRichMessage("<red><bold>Could not find a group with that uuid");
        } else {
            List<ItemDisplayGroup> groups = new ArrayList<>(ItemDisplayGroup.getAllItemDisplayGroups());
            for(ItemDisplayGroup group : groups){
                group.destroy();
            }
            return 0;
        }
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

    public static CompletableFuture<Suggestions> suggestGroupUuid(
            CommandContext<CommandSourceStack> context,
            SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining().toLowerCase();

        for (UUID uuid : ItemDisplayGroup.getAllUuids()) {
            if (uuid.toString().startsWith(remaining)) {
                builder.suggest(uuid.toString());
            }
        }

        return builder.buildFuture();
    }
}
