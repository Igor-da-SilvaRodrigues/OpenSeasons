package openseasons.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import net.minecraft.world.chunk.WorldChunk;
import openseasons.OpenSeasonsMod;
import openseasons.OpenSeasonsWorldState;
import openseasons.Seasons;

import java.util.Random;

import static net.minecraft.state.property.Properties.SNOWY;

public class WorldManipulation {

    /**
     * Melt blocks across a range of blocks. Necessary to melt any blocks that arent on the surface
     * @param chunk The target chunk
     * @param world The target World
     */
    public static void meltBlocksOnChunk(WorldChunk chunk, ServerWorld world){
        Random random = world.getRandom();
        BlockPos originBlockPos = chunk.getPos().getStartPos();
        BlockPos.Mutable mutableBlock = new BlockPos.Mutable();

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
                    if (random.nextInt(1000) == 1){//layer of randomness, this will make it so not all blocks will melt at the same time.

                        int x = originBlockPos.getX() + i;//getting the absolute coordinates
                        int z = originBlockPos.getZ() + k;

                        //blocks with non-solid neighbouring blocks are more likely to melt.
                        mutableBlock.set(x, y, z);
                        if (shouldMeltBlock(world, mutableBlock)){
                            meltBlock(world, mutableBlock);
                        }else if (random.nextInt(16) == 0 ){
                            meltBlock(world, mutableBlock);
                        }
                    }
                }
            }
        }


    }

    /**
     * melts a specific block
     * @param world The target World
     * @param pos The target Block
     */
    public static void meltBlock(ServerWorld world, BlockPos pos){
        BlockPos blockBelow = pos.down();
        BlockState blockState = world.getBlockState(pos);
        BlockState blockStateBelow = world.getBlockState(blockBelow);

        if (blockState == Blocks.SNOW.getDefaultState() || blockState == Blocks.SNOW_BLOCK.getDefaultState()){
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);

            //if there is snow, there might be a snowy grass block below.
            if (blockStateBelow == Blocks.GRASS_BLOCK.getDefaultState().with(SNOWY, true)){
                world.setBlockState(blockBelow, blockStateBelow.with(SNOWY, false), Block.NOTIFY_ALL);
            }
        }

        if (blockState == Blocks.ICE.getDefaultState() || blockState == Blocks.FROSTED_ICE.getDefaultState()){

            if (world.getDimension().isUltrawarm()) {
                world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
            } else {
                world.setBlockState(pos, Blocks.WATER.getDefaultState(), Block.NOTIFY_ALL);
            }
        }
    }

    //Ice should only be set in water, and only if the one of its neighbors is NOT water.
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


    public static boolean shouldMeltBlocks(ServerWorld world) {
        return OpenSeasonsMod.enableDynamicWeather && OpenSeasonsWorldState.getState(world).current_season != Seasons.WINTER;
    }

    public static boolean shouldMeltBlock(ServerWorld world, BlockPos pos) {

        if (!OpenSeasonsMod.enableDynamicWeather || OpenSeasonsWorldState.getState(world).current_season == Seasons.WINTER){
            return false;
        }

        if (pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY()){
                if (hasNonSolidNeighbor(world, pos)) {
                    return true;
                }
        }
        return false;

    }


    private static boolean hasNonSolidNeighbor(ServerWorld world, BlockPos pos){

        BlockPos b = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
        BlockPos east = new BlockPos(b.getX()+1, b.getY(), b.getZ());
        BlockPos west = new BlockPos(b.getX()-1, b.getY(), b.getZ());
        BlockPos north = new BlockPos(b.getX(), b.getY(), b.getZ()+1);
        BlockPos south = new BlockPos(b.getX(), b.getY(), b.getZ()-1);

        boolean isNotSolidEast = (world.getBlockState(east) == Blocks.WATER.getDefaultState() || world.getBlockState(east) == Blocks.AIR.getDefaultState());

        boolean isNotSolidWest = (world.getBlockState(west) == Blocks.WATER.getDefaultState() || world.getBlockState(west) == Blocks.AIR.getDefaultState());

        boolean isNotSolidNorth = (world.getBlockState(north) == Blocks.WATER.getDefaultState() || world.getBlockState(north) == Blocks.AIR.getDefaultState());

        boolean isNotSolidSouth = (world.getBlockState(south) == Blocks.WATER.getDefaultState() || world.getBlockState(south) == Blocks.AIR.getDefaultState());

        return (isNotSolidEast || isNotSolidWest || isNotSolidNorth || isNotSolidSouth);
    }


}
