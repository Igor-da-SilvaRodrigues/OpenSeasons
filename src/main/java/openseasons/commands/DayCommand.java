package openseasons.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class DayCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return 0;
    }

    public int set(CommandContext<ServerCommandSource> context) throws  CommandSyntaxException {
        return 0;
    }

    public void register(CommandDispatcher<ServerCommandSource> dispatcher){
        dispatcher.register(
                CommandManager.literal("openseasons")
                        .then(CommandManager.literal("day"))
                        .executes(this::run)
        );

        dispatcher.register(
                CommandManager.literal("openseasons")
                        .then(CommandManager.literal("day"))
                        .then(CommandManager.literal("set"))
                        .executes(this::set)
        );
    }

}
