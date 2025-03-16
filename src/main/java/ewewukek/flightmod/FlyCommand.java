package ewewukek.flightmod;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class FlyCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerFlyCommand(dispatcher);
        });
    }

    private static void registerFlyCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("fly")
                .requires(source -> source.hasPermissionLevel(4))
                .then(literal("add")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    return addFlyPlayer(context.getSource(), target);
                                })))
                .then(literal("remove")
                        .then(argument("player", EntityArgumentType.player())
                                .executes(context -> {
                                    ServerPlayerEntity target = EntityArgumentType.getPlayer(context, "player");
                                    return removeFlyPlayer(context.getSource(), target);
                                })))
        );
    }

    private static int addFlyPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        if (Config.playersAllowedToFly.contains(playerName)) {
            source.sendFeedback(Text.of(player.getName().getString() + " can already fly !"), false);
        } else {
            Config.playersAllowedToFly.add(playerName);
            source.sendFeedback(Text.of( player.getName().getString() + " can now fly !"), true);
            Config.playersAllowedToFlyHashMap.clear();
            Config.save();
        }

        return 1;
    }

    private static int removeFlyPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        if (!Config.playersAllowedToFly.contains(playerName)) {
            source.sendFeedback(Text.of(player.getName().getString() + " can't fly."), false);
        } else {
            Config.playersAllowedToFly.remove(playerName);
            source.sendFeedback(Text.of(player.getName().getString() + " can't fly anymore !"), true);
            Config.playersAllowedToFlyHashMap.clear();
            Config.save();
        }

        return 1;
    }
}
