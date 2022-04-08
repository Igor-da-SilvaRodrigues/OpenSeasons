package openseasons.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;
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
import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {

    @Shadow public abstract ServerWorld toServerWorld();

    @Inject(method = "tickChunk", at = @At(value = "HEAD"))
    private void tickChunkInject(WorldChunk chunk, int randomTickSpeed, CallbackInfo info){
        ServerWorld thisServerWorld = this.toServerWorld();
        Profiler profiler1 = thisServerWorld.getProfiler();
        Random random1 = thisServerWorld.getRandom();
        BlockPos blockPos1;
        ChunkPos chunkPos1 = chunk.getPos();
        int i1 = chunkPos1.getStartX();
        int j1 = chunkPos1.getStartZ();
        //profiler1.push("iceandsnow");
        if (random1.nextInt(16) == 0) {
            blockPos1 = thisServerWorld.getTopPosition(Heightmap.Type.MOTION_BLOCKING, thisServerWorld.getRandomPosInChunk(i1, 0, j1, 15));
            BlockPos blockPos2 = blockPos1.down();
            Biome biome = thisServerWorld.getBiome(blockPos1).value();


            if (WorldManipulation.shouldSetIce(thisServerWorld, blockPos2)) {

                thisServerWorld.setBlockState(blockPos2, Blocks.ICE.getDefaultState());
            }

            if (WorldManipulation.shouldMeltBlocks(thisServerWorld)){
                WorldManipulation.meltBlocksOnChunk(chunk, thisServerWorld);
            }
        }
       // profiler1.pop();
    }




/*
    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;canSetIce(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"), cancellable = true)
    private void tickChunkIceInject(WorldChunk chunk, int randomTickSpeed, CallbackInfo info){
       if (OpenSeasonsMod.currentSeason != Seasons.WINTER) {

           ServerWorld thisServerWorld = this.toServerWorld();
           ChunkPos chunkPos = chunk.getPos();
           int i = chunkPos.getStartX();
           int j = chunkPos.getStartZ();
           Random myRandom = thisServerWorld.random;

           Profiler profiler = thisServerWorld.getProfiler();
           profiler.push("tickBlocks");
           if (randomTickSpeed > 0){
               for (ChunkSection chunkSection : chunk.getSectionArray()) {
                   if (!chunkSection.hasRandomTicks()) continue;
                   int k = chunkSection.getYOffset();
                   for (int l = 0; l < randomTickSpeed; ++l) {
                       FluidState fluidState;
                       BlockPos blockPos3 = thisServerWorld.getRandomPosInChunk(i, k, j, 15);
                       profiler.push("randomTick");
                       BlockState blockState2 = chunkSection.getBlockState(blockPos3.getX() - i, blockPos3.getY() - k, blockPos3.getZ() - j);
                       if (blockState2.hasRandomTicks()) {
                           blockState2.randomTick(thisServerWorld, blockPos3, myRandom);
                       }
                       if ((fluidState = blockState2.getFluidState()).hasRandomTicks()) {
                           fluidState.onRandomTick(thisServerWorld, blockPos3, myRandom);
                       }
                       profiler.pop();
                   }
               }
           }
            profiler.pop();
            info.cancel();
       }
    }

    @Inject(method = "tickChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/biome/Biome;canSetSnow(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z"), cancellable = true)
    private void tickChunkSnowInject(WorldChunk chunk, int randomTickSpeed, CallbackInfo info){
        if (OpenSeasonsMod.currentSeason != Seasons.WINTER) {
            OpenSeasonsMod.LOGGER.info("current season is NOT winter, water won't freeze water");
            OpenSeasonsMod.LOGGER.info("It is actually, {}", OpenSeasonsMod.currentSeason);
            ServerWorld thisServerWorld = this.toServerWorld();
            ChunkPos chunkPos = chunk.getPos();
            int i = chunkPos.getStartX();
            int j = chunkPos.getStartZ();
            Random myRandom = thisServerWorld.random;

            Profiler profiler = thisServerWorld.getProfiler();
            profiler.push("tickBlocks");
            if (randomTickSpeed > 0){
                for (ChunkSection chunkSection : chunk.getSectionArray()) {
                    if (!chunkSection.hasRandomTicks()) continue;
                    int k = chunkSection.getYOffset();
                    for (int l = 0; l < randomTickSpeed; ++l) {
                        FluidState fluidState;
                        BlockPos blockPos3 = thisServerWorld.getRandomPosInChunk(i, k, j, 15);
                        profiler.push("randomTick");
                        BlockState blockState2 = chunkSection.getBlockState(blockPos3.getX() - i, blockPos3.getY() - k, blockPos3.getZ() - j);
                        if (blockState2.hasRandomTicks()) {
                            blockState2.randomTick(thisServerWorld, blockPos3, myRandom);
                        }
                        if ((fluidState = blockState2.getFluidState()).hasRandomTicks()) {
                            fluidState.onRandomTick(thisServerWorld, blockPos3, myRandom);
                        }
                        profiler.pop();
                    }
                }
            }
            profiler.pop();
            info.cancel();
        }else{
            OpenSeasonsMod.LOGGER.info("current season is winter, should freeze water");
        }
    }


 */
}
