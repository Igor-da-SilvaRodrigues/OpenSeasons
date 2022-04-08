package openseasons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerChunkEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import openseasons.JSON.SimpleJSON;
import openseasons.commands.DayCommand;
import openseasons.commands.OpenseasonsCommand;
import openseasons.commands.SeasonCommand;
import openseasons.util.Keys;
import openseasons.util.S2C;
import openseasons.util.WorldManipulation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

public class OpenSeasonsMod implements ModInitializer {
	public static final String MOD_ID = Keys.MOD_ID;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static byte MAX_DAY_COUNT = 10;
	public static boolean enableDynamicWeather = true;
	private static final String config_path = "config/openseasons.json";

	public static final Identifier RELOAD_RENDERER = new Identifier(MOD_ID,Keys.RELOAD_RENDERER);
	public static final Identifier SET_SEASON = new Identifier(MOD_ID,Keys.NEXT_SEASON);
	public static final Identifier UPDATE_BLOCK_STATE = new Identifier(MOD_ID, Keys.UPDATE_BLOCK_STATE);

	public static Seasons currentSeason = Seasons.SUMMER;

	@Override
	public void onInitialize() {

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			LOGGER.info("Loading seasons");
			load();
		});

		ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
			LOGGER.info("Saving seasons");
			save();
		});

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			LOGGER.info(MOD_ID + ": First time configuration. If this is the first time loading a world, expect it to" +
					" load " +
					"default configurations");
			OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(handler.getPlayer().getWorld());

			if (worldState.current_season != currentSeason) {
				LOGGER.info("Loading {} as the current season", worldState.current_season);
				currentSeason = worldState.current_season;
			}

			S2C.setClientSeason(worldState, handler.getPlayer());
		});

		//ServerWorldEvents.LOAD.register((server, world) -> {
		//	if(enableDynamicWeather){
		//		OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
		//		OpenSeasonsMod.currentSeason = worldState.current_season;
		//		LOGGER.info("Loading {} as the current season", worldState.current_season);
		//	}
		//});


		ServerTickEvents.END_WORLD_TICK.register((world)->{
			short currentTime = (short) (world.getTimeOfDay() % 24000L);//getting time of day

			if (currentTime == 1){
				boolean seasonChanged = false;
				OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
				worldState.current_day +=  1;

				LOGGER.info("A day has passed.");
				LOGGER.info("We're in day number {}", worldState.current_day);

				if (worldState.current_day > MAX_DAY_COUNT){
					worldState.current_season = worldState.current_season.next();
					worldState.current_day = 1;

					seasonChanged = true;
				}
				LOGGER.info("Trying to set state");
				OpenSeasonsWorldState.setState(world, worldState, seasonChanged);
			}


			//tryMeltBlocks(world);

		});//tick

		registerCommands();

	}

	// register commands here....
	private void registerCommands(){
		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {

			new OpenseasonsCommand().register(dispatcher);
			new DayCommand().register(dispatcher);
			new SeasonCommand().register(dispatcher);

		});
	}


	static void load(){
		JsonElement element;
		try{
			JsonObject object = SimpleJSON.loadFrom(config_path);

			element = object.get(Keys.MAX_DAY_COUNT);

			if(element != null && element.isJsonPrimitive()) {
				MAX_DAY_COUNT = element.getAsJsonPrimitive().getAsByte();
				LOGGER.info(MOD_ID +":Loaded {} as the current day limit",MAX_DAY_COUNT);
			}

			element = object.get(Keys.SUMMER_COLOR);
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.SUMMER.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Summer color",color);
			}

			element = object.get(Keys.FALL_COLOR);
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.FALL.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Fall color",color);
			}

			element = object.get(Keys.WINTER_COLOR);
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.WINTER.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Winter color",color);
			}

			element = object.get(Keys.SPRING_COLOR);
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.SPRING.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Spring color",color);
			}

			element = object.get(Keys.ENABLE_DYNAMIC_WEATHER);
			if (element != null && element.isJsonPrimitive()){
				enableDynamicWeather = element.getAsJsonPrimitive().getAsBoolean();
				if (enableDynamicWeather){
					LOGGER.warn(MOD_ID + ": Enabled Dynamic Weather! Don't use multiple worlds!!");
				}
			}

		}catch (IOException e){
			LOGGER.error(MOD_ID +":Failed to load configuration\n"+ e);
		}
	}

	static void save() {
		JsonObject attributes = new JsonObject();

		attributes.addProperty(Keys.MAX_DAY_COUNT, MAX_DAY_COUNT);
		attributes.addProperty(Keys.SUMMER_COLOR, String.valueOf(Seasons.SUMMER.getFoliagecolor()));
		attributes.addProperty(Keys.FALL_COLOR,   String.valueOf(Seasons.FALL.getFoliagecolor()));
		attributes.addProperty(Keys.WINTER_COLOR, String.valueOf(Seasons.WINTER.getFoliagecolor()));
		attributes.addProperty(Keys.SPRING_COLOR, String.valueOf(Seasons.SPRING.getFoliagecolor()));
		attributes.addProperty(Keys.ENABLE_DYNAMIC_WEATHER, enableDynamicWeather);

		try{
			SimpleJSON.saveTo(config_path, attributes);
		}catch(IOException e){
			LOGGER.error(MOD_ID +":Failed to save configuration\n"+e);
		}
	}
}