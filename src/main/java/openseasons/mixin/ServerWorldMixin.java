package openseasons.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import openseasons.OpenSeasonsMod;
import openseasons.Seasons;
import openseasons.util.WorldManipulation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

import static net.minecraft.state.property.Properties.SNOWY;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow public abstract ServerWorld toServerWorld();

    @Inject(method = "tickChunk", at = @At(value = "HEAD"))
    private void tickChunkInject(WorldChunk chunk, int randomTickSpeed, CallbackInfo info){
        if (OpenSeasonsMod.enableDynamicWeather){
            ServerWorld thisServerWorld = this.toServerWorld();
            Random random1 = thisServerWorld.getRandom();
            BlockPos blockPos1;
            ChunkPos chunkPos1 = chunk.getPos();
            int i1 = chunkPos1.getStartX();
            int j1 = chunkPos1.getStartZ();
            //freezing water and melting ice and snow.
            if (random1.nextInt(16) == 0) {
                blockPos1 = thisServerWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, thisServerWorld.getRandomPosInChunk(i1, 0, j1, 15));
                BlockPos blockPos2 = blockPos1.down();

                if (WorldManipulation.shouldSetIce(thisServerWorld, blockPos2)) {
                    thisServerWorld.setBlockState(blockPos2, Blocks.ICE.getDefaultState());
                }

                if (WorldManipulation.shouldMeltBlocks(thisServerWorld)) {
                    WorldManipulation.meltBlocksOnChunk(chunk, thisServerWorld);
                }

                if (thisServerWorld.isRaining()) {
                    if (OpenSeasonsMod.currentSeason == Seasons.WINTER) {
                        thisServerWorld.setBlockState(blockPos1, Blocks.SNOW.getDefaultState());
                    }
                }
            }
        }
    }


}
