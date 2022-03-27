package openseasons.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import openseasons.OpenSeasonsMod;
import openseasons.OpenSeasonsWorldState;

public class DayCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        ServerWorld world = context.getSource().getWorld();
        OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
        int day = worldState.current_day;
        context.getSource().getPlayer().sendSystemMessage(new LiteralText("We're in day number " + day), Util.NIL_UUID);

        return 0;
    }

    public int set(CommandContext<ServerCommandSource> context) throws  CommandSyntaxException {

        int day = IntegerArgumentType.getInteger(context, "day");

        if (day < 1 || day > OpenSeasonsMod.MAX_DAY_COUNT){
            throw new SimpleCommandExceptionType(
                    new LiteralText("Day argument out of range: 1-" +OpenSeasonsMod.MAX_DAY_COUNT)
            ).create();
        }

        ServerWorld world = context.getSource().getWorld();
        OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
        worldState.current_day = (byte) day;
        OpenSeasonsWorldState.setState(world, worldState, false);

        return 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher){

        dispatcher.register(
                CommandManager.literal("openseasons").requires(source -> source.hasPermissionLevel(2)).then(
                        CommandManager.literal("day").then(
                                CommandManager.literal("set").requires(source -> source.hasPermissionLevel(4)).then(
                                        CommandManager.argument("day", IntegerArgumentType.integer())
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
