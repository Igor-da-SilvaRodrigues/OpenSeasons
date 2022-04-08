package openseasons.util;

import net.minecraft.block.*;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LightType;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import openseasons.OpenSeasonsMod;
import openseasons.OpenSeasonsWorldState;
import openseasons.Seasons;

import java.util.Random;

import static net.minecraft.state.property.Properties.SNOWY;
import static openseasons.OpenSeasonsMod.LOGGER;

public class WorldManipulation {

    /**
     * <p>Melt blocks in a world. This method will check a certain range of block coordinates and use {@link Random#nextInt(int)} to give a chance for each block to melt. </p>
     * <p>Meltable blocks include:
     * <ul>
     *     <li>Snow</li>
     *     <li>Snow Block</li>
     *     <li>Ice</li>
     *     <li>Snowy Grass Block</li>
     * </ul>
     *</p>
     * @param world The target world
     * @param chunk The target chunk
     */
    public static void meltBlocksOnChunk(WorldChunk chunk, ServerWorld world){
        Random random = world.getRandom();
        BlockPos originBlockPos = chunk.getPos().getStartPos();
        BlockPos.Mutable mutableBlock = new BlockPos.Mutable();
        BlockPos.Mutable mutableBlockBelow = new BlockPos.Mutable();

        int bottom = chunk.getBottomY();
        int top = chunk.getTopY();
        if (top > 100) {
            top = 100;
        }
        if (bottom < 40) {
            bottom = 40;
        }//no I'm not using the ternary operator...
        //looping in a 8x8 chunks square around the player chunk
        for (int i = -63; i < 64; i += 1){
            for (int y = bottom ;y <= top ; y += 1){//very high mountains will get to keep their snow cover in all
                // seasons.
                for (int k = -63; k < 64; k += 1){
                    if (random.nextInt(1000) == 1){// second layer of randomness, this will make it so not all blocks will melt at the same time.

                        int x = originBlockPos.getX() + i;//getting the absolute coordinates
                        int z = originBlockPos.getZ() + k;

                        mutableBlock.set(x, y, z);
                        mutableBlockBelow.set(mutableBlock).move(Direction.DOWN, 1);

                        BlockState blockState = world.getBlockState(mutableBlock);
                        BlockState blockStateBelow = world.getBlockState(mutableBlockBelow);

                        if (blockState == Blocks.SNOW.getDefaultState() || blockState == Blocks.SNOW_BLOCK.getDefaultState()){
                            world.setBlockState(mutableBlock, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

                            //if there is snow, there might be a snowy grass block below.
                            if (blockStateBelow == Blocks.GRASS_BLOCK.getDefaultState().with(SNOWY, true)){
                                world.setBlockState(mutableBlockBelow, blockStateBelow.with(SNOWY, false), Block.NOTIFY_ALL);
                            }
                        }
                        //melting ice...
                        if (blockState == Blocks.ICE.getDefaultState() || blockState == Blocks.FROSTED_ICE.getDefaultState()){

                            if (world.getDimension().isUltrawarm()) {
                                world.setBlockState(mutableBlock, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                            } else {
                                world.setBlockState(mutableBlock, Blocks.WATER.getDefaultState(), Block.NOTIFY_ALL);
                            }
                        }
                    }
                }
            }
        }


    }

    public static boolean shouldSetIce(WorldView world, BlockPos pos){
        if (OpenSeasonsMod.currentSeason == Seasons.WINTER){

            if (pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, pos) < 10){
                FluidState fluid = world.getFluidState(pos);
                if (fluid.getFluid() == Fluids.WATER){

                    BlockPos b = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                    BlockPos east = new BlockPos(b.getX()+1, b.getY(), b.getZ());
                    BlockPos west = new BlockPos(b.getX()-1, b.getY(), b.getZ());
                    BlockPos north = new BlockPos(b.getX(), b.getY(), b.getZ()+1);
                    BlockPos south = new BlockPos(b.getX(), b.getY(), b.getZ()-1);

                    boolean isSolidEast = !(world.getBlockState(east) == Blocks.WATER.getDefaultState());
                    boolean isSolidWest = !(world.getBlockState(west) == Blocks.WATER.getDefaultState());
                    boolean isSolidNorth = !(world.getBlockState(north) == Blocks.WATER.getDefaultState());
                    boolean isSolidSouth = !(world.getBlockState(south) == Blocks.WATER.getDefaultState());
                    boolean hasSolidNeighbor = (isSolidEast || isSolidSouth || isSolidWest || isSolidNorth);
                    if (hasSolidNeighbor) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean shouldMeltBlocks(ServerWorld world){
        if (!OpenSeasonsMod.enableDynamicWeather || OpenSeasonsWorldState.getState(world).current_season == Seasons.WINTER){
            return false;
        }
        return true;
    }
}
