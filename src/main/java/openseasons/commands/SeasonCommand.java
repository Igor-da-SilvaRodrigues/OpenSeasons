package openseasons.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import openseasons.OpenSeasonsWorldState;
import openseasons.Seasons;

public class SeasonCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerWorld world = context.getSource().getWorld();
        Seasons season = OpenSeasonsWorldState.getState(world).current_season;

        ServerPlayerEntity player = context.getSource().getPlayer();
        player.sendSystemMessage(new LiteralText("It's a "+season+" day"), Util.NIL_UUID);

        return 0;
    }

    public int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        String season = StringArgumentType.getString(context,"season");

        if (!Seasons.hasSeason(season)){
            throw new SimpleCommandExceptionType(
                    new LiteralText("Season argument does not exist")
            ).create();
        }

        ServerWorld world = context.getSource().getWorld();
        OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
        worldState.current_season = Seasons.getSeason(season);
        OpenSeasonsWorldState.setState(world, worldState, true);
        return 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("openseasons").requires(source -> source.hasPermissionLevel(2)).then(
                        CommandManager.literal("season").then(
                                CommandManager.literal("set").requires(source -> source.hasPermissionLevel(4)).then(
                                        CommandManager.argument("season", StringArgumentType.string())
                                                .executes(this::set)
                                )
                        ).then(
                                CommandManager.literal("query")
                                        .executes(this)
                        )
                )
        );
    }
}
