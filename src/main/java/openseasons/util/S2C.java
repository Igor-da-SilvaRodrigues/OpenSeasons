package openseasons.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import openseasons.OpenSeasonsWorldState;

import static openseasons.OpenSeasonsMod.UPDATE_BLOCK_STATE;
import static openseasons.OpenSeasonsMod.SET_SEASON;

public class S2C {
    /**
     * Notifies a client to update a Block State.
     * @param blockPos The target block position in the target chunk
     * @param blockState The desired new block state
     * @param player The client that will receive the update.
     */
    public static void updateClientBlock(BlockPos blockPos, BlockState blockState, ServerPlayerEntity player){
        BlockUpdateS2CPacket blockUpdatePacket = new BlockUpdateS2CPacket(blockPos, blockState);

        PacketByteBuf buf = PacketByteBufs.create();
        blockUpdatePacket.write(buf);

        ServerPlayNetworking.send(player, UPDATE_BLOCK_STATE, buf);
    }

    /**
     * Notifies all clients in a world to set a season and reload their world renderer. Needs to be called for a season to render.
     * @param world The target world
     * @param worldState The state containing the desired season.
     * @see S2C#setClientSeason(OpenSeasonsWorldState, ServerPlayerEntity)
     */
    public static void setAllClientsSeason(ServerWorld world, OpenSeasonsWorldState worldState){
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeString(worldState.current_season.toString());

        for (ServerPlayerEntity player : world.getPlayers()){
            ServerPlayNetworking.send(player, SET_SEASON, buffer);
        }

    }

    /**
     * Notifies a client to set a season and reload their world renderer. Needs to be called for a season to render.
     * @param worldState The state containing the desired season
     * @param player The target client
     * @see S2C#setAllClientsSeason(ServerWorld, OpenSeasonsWorldState)
     */
    public static void setClientSeason(OpenSeasonsWorldState worldState, ServerPlayerEntity player){
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeString(worldState.current_season.toString());
        ServerPlayNetworking.send(player, SET_SEASON, buffer);
    }



}
