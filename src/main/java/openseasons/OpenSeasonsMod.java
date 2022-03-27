package openseasons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import openseasons.JSON.SimpleJSON;
import openseasons.util.Keys;
import openseasons.commands.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OpenSeasonsMod implements ModInitializer {
	public static final String MOD_ID = Keys.MOD_ID;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static byte MAX_DAY_COUNT = 10;
	private static final String config_path = "config/openseasons.json";

	public static final Identifier RELOAD_RENDERER = new Identifier(MOD_ID,Keys.RELOAD_RENDERER);
	public static final Identifier NEXT_SEASON = new Identifier(MOD_ID,Keys.NEXT_SEASON);
	public static final Identifier CLIENT_JOIN = new Identifier(MOD_ID, Keys.CLIENT_JOIN);

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
			LOGGER.info("First time configuration. If this is the first time loading a world, expect it to load " +
					"default configurations");
			OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(handler.getPlayer().getWorld());

			PacketByteBuf buf = PacketByteBufs.create();
			buf.writeString(worldState.current_season.toString());

			ServerPlayNetworking.send(handler.getPlayer(), CLIENT_JOIN, buf);

		});


		ServerTickEvents.END_WORLD_TICK.register((world)->{
			short currentTime = (short) (world.getTimeOfDay() % 24000L);//getting time of day

			if (currentTime == 1){
				OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
				worldState.current_day +=  1;

				LOGGER.info("A day has passed.");
				LOGGER.info("We're in day number {}", worldState.current_day);

				if (worldState.current_day > MAX_DAY_COUNT){
					worldState.current_season = worldState.current_season.next();
					worldState.current_day = 1;
					reloadSeason(world, worldState);

				}
				LOGGER.info("Trying to set state");
				worldState.markDirty();
				world.getPersistentStateManager().set(Keys.WORLD_STATE, worldState);//set state every time a day passes.

			}

		});

		registerCommands();

	}

	/**
	 * Notifies all clients to reload their world renderer. Needs to be called for a season to render.
	 * @param world You know what this is
	 * @param worldState The state containing the desired season.
	 */
	static void reloadSeason(ServerWorld world, OpenSeasonsWorldState worldState){

		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeString(worldState.current_season.toString());

		for (ServerPlayerEntity player : world.getPlayers()){
			ServerPlayNetworking.send(player, NEXT_SEASON, buffer);
		}

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

			element = object.get("max_day_count");

			if(element != null && element.isJsonPrimitive()) {
				MAX_DAY_COUNT = element.getAsJsonPrimitive().getAsByte();
				LOGGER.info("Loaded {} as the current day limit",MAX_DAY_COUNT);
			}

			element = object.get("summer_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.SUMMER.setFoliagecolor(color);
				LOGGER.info("Loaded {} as the Summer color",color);
			}

			element = object.get("fall_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.FALL.setFoliagecolor(color);
				LOGGER.info("Loaded {} as the Fall color",color);
			}

			element = object.get("winter_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.WINTER.setFoliagecolor(color);
				LOGGER.info("Loaded {} as the Winter color",color);
			}

			element = object.get("spring_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.SPRING.setFoliagecolor(color);
				LOGGER.info("Loaded {} as the Spring color",color);
			}
		}catch (IOException e){
			LOGGER.error("Failed to load configuration\n"+ e);
		}
	}

	static void save() {
		JsonObject attributes = new JsonObject();

		attributes.addProperty("max_day_count", MAX_DAY_COUNT);
		attributes.addProperty("summer_color", String.valueOf(Seasons.SUMMER.getFoliagecolor()));
		attributes.addProperty("fall_color",   String.valueOf(Seasons.FALL.getFoliagecolor()));
		attributes.addProperty("winter_color", String.valueOf(Seasons.WINTER.getFoliagecolor()));
		attributes.addProperty("spring_color", String.valueOf(Seasons.SPRING.getFoliagecolor()));

		try{
			SimpleJSON.saveTo(config_path, attributes);
		}catch(IOException e){
			LOGGER.error("Failed to save configuration\n"+e);
		}
	}
}