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
                .then(literal("removeAll")
                        .executes(context -> removeFlyPlayers(context.getSource())))
                .then(literal("list")
                        .executes(context -> GetAllowedPlayerList(context.getSource())))

        );
    }

    private static int addFlyPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        if (Config.playersAllowedToFly.contains(playerName)) {
            source.sendFeedback(Text.of(playerName + " can already fly !"), false);
        } else {
            Config.playersAllowedToFly.add(playerName);
            source.sendFeedback(Text.of(playerName + " can now fly !"), true);
            Config.playersAllowedToFlyHashMap.clear();
            Config.save();
        }

        return 1;
    }

    private static int removeFlyPlayer(ServerCommandSource source, ServerPlayerEntity player) {
        String playerName = player.getName().getString();

        if (!Config.playersAllowedToFly.contains(playerName)) {
            source.sendFeedback(Text.of(playerName + " can't fly."), false);
        } else {
            Config.playersAllowedToFly.remove(playerName);
            source.sendFeedback(Text.of(playerName + " can't fly anymore !"), true);
            Config.playersAllowedToFlyHashMap.clear();
            Config.save();
        }

        return 1;
    }

    private static int removeFlyPlayers(ServerCommandSource source) {
        Config.playersAllowedToFly.clear();
        Config.playersAllowedToFlyHashMap.clear();
        Config.save();
        source.sendFeedback(Text.of("List of players allowed to fly has been clean"), true);
        return 1;
    }

    private static int GetAllowedPlayerList(ServerCommandSource source) {
        if (Config.playersAllowedToFly.isEmpty()) {
            source.sendFeedback(Text.of("List of players allowed to fly is empty"), false);
        } else {
            source.sendFeedback(Text.of("All players allowed to fly: " + String.join(",", Config.playersAllowedToFly)), false);
        }
        return 1;
    }
}
