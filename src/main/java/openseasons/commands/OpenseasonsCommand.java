package openseasons.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import openseasons.OpenSeasonsMod;
import openseasons.OpenSeasonsWorldState;
import openseasons.Seasons;
import openseasons.util.Keys;

public class OpenseasonsCommand implements Command<ServerCommandSource> {

    // openseasons
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerWorld world = context.getSource().getWorld();

        OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);

        int currentDay = worldState.current_day;
        Seasons currentSeason = worldState.current_season;

        player.sendSystemMessage(new LiteralText("We're in day number " + currentDay), Util.NIL_UUID);
        player.sendSystemMessage(new LiteralText("It's a "+currentSeason+" day"), Util.NIL_UUID);

        return 0;
    }

    public int set(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        Integer day = IntegerArgumentType.getInteger(context, "day");
        if (day < 1 || day >= OpenSeasonsMod.MAX_DAY_COUNT) {
            throw new SimpleCommandExceptionType(
                    new LiteralText("Day argument out of range: 1-" +(OpenSeasonsMod.MAX_DAY_COUNT - 1))
            ).create();

        }

        String season = StringArgumentType.getString(context, "season");
        if (!Seasons.hasSeason(season)){
            throw new SimpleCommandExceptionType(
                    new LiteralText("Season argument does not exist")
            ).create();
        }

        ServerWorld world = context.getSource().getWorld();
        OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
        worldState.current_season = Seasons.getSeason(season);
        worldState.current_day = day.byteValue();
        OpenSeasonsWorldState.setState(world, worldState);

        return 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("openseasons").then(
                        CommandManager.literal("set").then(
                                CommandManager.argument("season", StringArgumentType.string()).then(
                                        CommandManager.argument("day", IntegerArgumentType.integer())
                                                .executes(this::set)
                                )
                        )
                ).then(
                        CommandManager.literal("query")
                                .executes(this)
                )


                /*
                .then((CommandManager.literal("set"))
                .then((CommandManager.argument("season", StringArgumentType.string()))
                .then(CommandManager.argument("day", IntegerArgumentType.integer()))))
                .executes(this::set)

                 */
        );

    }

}
