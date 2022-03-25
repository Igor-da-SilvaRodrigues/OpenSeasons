package openseasons.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;
import openseasons.OpenSeasonsWorldState;
import openseasons.Seasons;

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

        return 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("openseasons")
                        .executes(this::run)
        );

        dispatcher.register(
                CommandManager.literal("openseasons")
                        .then(CommandManager.literal("set"))
                        .executes(this::set)
        );
    }
}
