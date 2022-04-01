package openseasons.util;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SnowyBlock;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.WorldChunk;
import openseasons.OpenSeasonsMod;
import openseasons.OpenSeasonsWorldState;

import java.util.Random;

import static net.minecraft.state.property.Properties.SNOWY;
import static openseasons.OpenSeasonsMod.LOGGER;
import static openseasons.util.S2C.updateClientBlock;

public class WorldManipulation {

    /**
     * <p>Melt blocks in a world. This method will check a certain range of block coordinates and use {@link Random#nextInt(int)} to give a 10% chance for each block to melt. </p>
     * <p>This method will only melt blocks in chunks occupied by a player.</p>
     * <p>This method will notify clients to update any affected block states.</p>
     * <p>Meltable blocks include:
     * <ul>
     *     <li>Snow</li>
     *     <li>Snow Block</li>
     *     <li>Ice</li>
     *     <li>Snowy Grass Block</li>
     * </ul>
     *</p>
     * @param world The target world
     *@see openseasons.util.S2C#updateClientBlock(BlockPos, BlockState, ServerPlayerEntity)
     */
    public static void meltBlocks(ServerWorld world){
        Random random = new Random();

        for (ServerPlayerEntity player : world.getPlayers()){
            WorldChunk chunk = world.getWorldChunk(player.getBlockPos()); // chunk where the player stands;
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

            for (int i = 0; i < 16; i += 1){
                for (int y = bottom ;y <= top ; y += 1){//very high mountains will get to keep their snow cover in all
                    // seasons.
                    for (int k = 0; k < 16; k += 1){
                        if (random.nextInt(10) == 1){// second layer of randomness, this will make it so not all blocks will melt at the same time.

                            int x = originBlockPos.getX() + i;//getting the absolute coordinates
                            int z = originBlockPos.getZ() + k;

                            mutableBlock.set(x, y, z);
                            mutableBlockBelow.set(mutableBlock).move(Direction.DOWN, 1);

                            BlockState blockState = world.getBlockState(mutableBlock);
                            BlockState blockStateBelow = world.getBlockState(mutableBlockBelow);

                            if (blockState == Blocks.SNOW.getDefaultState() || blockState == Blocks.SNOW_BLOCK.getDefaultState()){
                                LOGGER.info("Found snow");
                                world.setBlockState(mutableBlock, Blocks.AIR.getDefaultState(), Block.NOTIFY_ALL);
                                updateClientBlock( mutableBlock.toImmutable(), Blocks.AIR.getDefaultState(), player);

                                //if there is snow, there might be a snowy grass block below.
                                if (blockStateBelow == Blocks.GRASS_BLOCK.getDefaultState().with(SNOWY, true)){
                                    LOGGER.info("Found snowy grass");
                                    world.setBlockState(mutableBlockBelow, blockStateBelow.with(SNOWY, false), Block.NOTIFY_ALL);
                                    updateClientBlock(mutableBlockBelow.toImmutable(), blockStateBelow.with(SNOWY, false), player);
                                }


                            }
                        }
                    }
                }
            }
        }
    }//melt blocks
}
