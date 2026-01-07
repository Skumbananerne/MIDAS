package org.dopelegend.multiItemDisplayEngine.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
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
import org.dopelegend.multiItemDisplayEngine.rotation.Rotate;
import org.dopelegend.multiItemDisplayEngine.utils.classes.Triple;

import java.io.File;
import java.util.concurrent.CompletableFuture;

public class ModelCommand {
    public static int spawnModelByNameCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red> <bold> Only players can execute this command");
            return 0;
        }

        ItemDisplayGroup itemDisplayGroup = new ItemDisplayGroup(new Location(player.getWorld(), 0.5, 100.5 ,0.5), ctx.getArgument("model name", String.class));
        itemDisplayGroup.spawn();

        new BukkitRunnable() {
            @Override
            public void run() {
                itemDisplayGroup.teleportRelativeSmooth(new Triple(1, 0, 0), 5);
            }
        }.runTaskTimer(MultiItemDisplayEngine.plugin, 3, 5);

        return 1;
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
