package openseasons;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.client.color.world.FoliageColors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSeasonsMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("openseasons");

	private byte current_day = 1;
	private final byte MAX_DAY_COUNT = 30;
	private Seasons current_season = Seasons.SUMMER;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			this.load();
			//May or may not need OpenSeasonsUtil.reloadChunks();
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			this.save();
		});

		ServerTickEvents.END_WORLD_TICK.register((world)->{

			short currentTime = (short) (world.getTimeOfDay() % 24000L);//getting time of day

			if (currentTime == 0){
				this.current_day += 1;
				LOGGER.info("A day has passed\n");
				LOGGER.info("We're in day number {}\n", current_day);
				LOGGER.info("It's a {} day\n", current_season);
				LOGGER.info("Temperature: {}", world.getBiome(world.getRandomPosInChunk(0, 0, 0, 15)).value().getTemperature());

				if (this.current_day >= MAX_DAY_COUNT){
					//this.nextSeason(world);
				}
				this.nextSeason(world);//is here for ease of debugging.
			}
		});
	}

	private void nextSeason(ServerWorld world){
		//this function should set the next season in the following order:
		//...->Summer->Fall->Winter->Spring->...

		this.current_season = this.current_season.next();
		LOGGER.info("It is now {}", current_season);

		/*
		Biome biome = world.getBiome(world.getRandomPosInChunk(0, 0, 0, 15)).value(); //this will return a Biome object with getTemperature method.

		Biome.Builder newBiomeBuilder = Biome.Builder.copy(biome);
		newBiomeBuilder.temperature(current_season.getTemperature());
		biome = newBiomeBuilder.build();
		LOGGER.info("Setting temperature to {}", biome.getTemperature());

		 */


		OpenSeasonsUtil.setSeasonBlocks(world ,this, LOGGER);
		OpenSeasonsUtil.reloadChunks();
		LOGGER.info("The color should have been applied now!");

		this.current_day = 1;
	}

	/**
	 * Loads the current configuration from storage.
	 */
	private void load(){

	}

	/**
	 * Saves the current configuration to storage
	 */
	private void save(){

	}


	public Seasons getCurrent_season(){
		return this.current_season;
	}
}

@Environment(EnvType.CLIENT)
abstract class OpenSeasonsUtil {
	/**
	 * This function should set the new blocks for the current season, it'll have to either swap blocks like foliage and wood types, or it'll make changes on their rendering color.
	 * @param mod_instance :might not actually be necessary, but it's here for now
	 * @param LOGGER
	 */
	public static void setSeasonBlocks(ServerWorld world, OpenSeasonsMod mod_instance, Logger LOGGER){
		//int color = FoliageColors.getColor(current_season.getTemperature(), current_season.getHumidity());
		int intended_color = FoliageColors.getColor(mod_instance.getCurrent_season().getTemperature(), mod_instance.getCurrent_season().getHumidity());

		LOGGER.info("The following color has been chosen> {}", intended_color);

		/*
		Block[] leaves ={Blocks.DARK_OAK_LEAVES,
						Blocks.ACACIA_LEAVES,
						Blocks.FLOWERING_AZALEA_LEAVES,
						Blocks.OAK_LEAVES,
						Blocks.BIRCH_LEAVES,
						Blocks.AZALEA_LEAVES,
						Blocks.JUNGLE_LEAVES,
						Blocks.SPRUCE_LEAVES};

		 */
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


	public static void reloadChunks() {
		MinecraftClient.getInstance().reloadResources();
	}

}
