package openseasons.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import static openseasons.OpenSeasonsMod.RELOAD_BLOCK_STATE;


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

        ServerPlayNetworking.send(player, RELOAD_BLOCK_STATE, buf);
    }
}
