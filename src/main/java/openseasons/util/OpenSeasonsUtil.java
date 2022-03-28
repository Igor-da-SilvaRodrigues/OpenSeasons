package openseasons.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.block.Blocks;
import openseasons.OpenSeasonsMod;
import openseasons.Seasons;

@Environment(EnvType.CLIENT)
public class OpenSeasonsUtil {
    /**
     * sets the chosen season color.
     *
     */
    public static void setSeasonBlocks(Seasons season) {

        int intended_color = season.getFoliagecolor();

        OpenSeasonsMod.LOGGER.info(Keys.MOD_ID +"The following color has been chosen> {}", intended_color);

        ColorProviderRegistry.BLOCK.register((state, view, pos, tintIndex) -> intended_color, Blocks.DARK_OAK_LEAVES,
                Blocks.ACACIA_LEAVES,
                Blocks.FLOWERING_AZALEA_LEAVES,
                Blocks.OAK_LEAVES,
                Blocks.BIRCH_LEAVES,
                Blocks.AZALEA_LEAVES,
                Blocks.JUNGLE_LEAVES,
                Blocks.SPRUCE_LEAVES,
                Blocks.GRASS,
                Blocks.GRASS_BLOCK);
    }
}