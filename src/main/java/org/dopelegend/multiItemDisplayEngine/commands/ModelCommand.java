package org.dopelegend.multiItemDisplayEngine.commands;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.dopelegend.multiItemDisplayEngine.MultiItemDisplayEngine;
import org.dopelegend.multiItemDisplayEngine.blockBench.generator.TexturePack;
import org.dopelegend.multiItemDisplayEngine.itemDisplay.utils.itemDisplayGroups.ItemDisplayGroup;
import org.dopelegend.multiItemDisplayEngine.texturePack.PackWebServer;

public class ModelCommand {
    public static int spawnModelByNameCommand(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            ctx.getSource().getSender().sendRichMessage("<red> <bold> Only players can execute this command");
            return 0;
        }

        ItemDisplayGroup itemDisplayGroup = new ItemDisplayGroup(new Location(player.getWorld(), 0.5, 100.5 ,0.5), ctx.getArgument("model name", String.class));
        itemDisplayGroup.Spawn();
        return 1;
    }

    public static int generateTexturePack(CommandContext<CommandSourceStack> ctx) {
        if (!(ctx.getSource().getSender() instanceof Player)){
            ctx.getSource().getSender().sendRichMessage("<red> <bold> Only players can execute this command");
            return 0;
        }
        TexturePack.generateTexturePack();
        MultiItemDisplayEngine.packWebServer.refreshTexturePack();
        return 1;
    }
}
