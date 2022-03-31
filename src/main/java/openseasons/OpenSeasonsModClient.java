package openseasons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.tick.OrderedTick;
import openseasons.util.Keys;
import openseasons.util.OpenSeasonsUtil;

@Environment(EnvType.CLIENT)
public class OpenSeasonsModClient extends OpenSeasonsMod implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        OpenSeasonsMod.LOGGER.info(Keys.MOD_ID+ ":Initializing Client...");

        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.CLIENT_JOIN, (client, handler, buf,
                                                                                 responseSender) -> {
            Seasons season = Seasons.getSeason(buf.readString());
            OpenSeasonsUtil.setSeasonBlocks(season);

            client.execute(()->{
                client.worldRenderer.reload();
            });
        });

        //register packet receiver
        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.NEXT_SEASON, (client, handler, buf,
                                                                                 responseSender) -> {
            Seasons season = Seasons.getSeason(buf.readString());
            OpenSeasonsUtil.setSeasonBlocks(season);

            client.execute(()->{
               client.worldRenderer.reload();
            });

        });

        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.RELOAD_RENDERER, (client, handler, buf,
                                                                                     responseSender) -> {
            client.execute(()->{
                client.worldRenderer.reload();
            });

        });

        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.RELOAD_BLOCK_STATE, (client, handler, buf, responseSender) -> {
            BlockUpdateS2CPacket blockUpdatePacket = new BlockUpdateS2CPacket(buf);
            ChunkPos chunkPos = buf.readChunkPos();//the position of the target chunk
            BlockPos chunkStartPos = chunkPos.getStartPos();//need this because I can't get a chunk from Chunk Pos directly... bruh
            BlockPos blockPos = blockUpdatePacket.getPos();//the block to be updated.
            BlockState blockState = blockUpdatePacket.getState();//the new block state
            WorldChunk chunk = handler.getWorld().getWorldChunk(chunkStartPos);// the target chunk

           client.execute(()->{
               chunk.setBlockState(blockPos, blockState, false);//

           });

        });

    }
}
