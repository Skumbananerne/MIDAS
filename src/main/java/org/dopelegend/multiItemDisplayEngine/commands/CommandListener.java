package org.dopelegend.multiItemDisplayEngine.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;

import static org.dopelegend.multiItemDisplayEngine.commands.ModelCommand.*;

public class CommandListener {

    public CommandListener(Plugin plugin) {
        LifecycleEventManager<Plugin> lifecycleEventManager = plugin.getLifecycleManager();

        lifecycleEventManager.registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(modelCommand());
        });
    }

    private LiteralCommandNode<CommandSourceStack> modelCommand(){
        return Commands.literal("model")
                .requires(ctx -> ctx.getSender().isOp())
                .then(Commands.literal("spawn")
                        .then(Commands.argument("model name", StringArgumentType.greedyString())
                                .suggests(ModelCommand::suggestModels)
                                .executes(ModelCommand::spawnModelByNameCommand)
                        )
                )
                .then(Commands.literal("clear")
                        .executes(ctx -> {return deleteItemDisplayGroup(ctx, false);})
                        .then(Commands.argument("group uuid", StringArgumentType.greedyString())
                                .suggests(ModelCommand::suggestGroupUuid)
                                .executes(ctx -> {return deleteItemDisplayGroup(ctx, true);})
                        )
                )
                .then(Commands.literal("texturepack")
                        .then(Commands.literal("generate")
                                .executes(ModelCommand::generateTexturePack)))
                .then(Commands.literal("spawn_test")
                        .executes(ModelCommand::spawnTestItemDisplay)
                )
                .then(Commands.literal("test")
                        .executes(ModelCommand::testItemDisplay)
                ).build();

    }
}
