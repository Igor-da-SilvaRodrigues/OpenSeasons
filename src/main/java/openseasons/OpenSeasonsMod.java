package openseasons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import openseasons.JSON.SimpleJSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class OpenSeasonsMod implements ModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger("open-seasons");
	public static byte current_day = 1;
	public static byte MAX_DAY_COUNT = 10;
	static Seasons current_season = Seasons.SUMMER;
	private static final String config_path = "config/openseasons.json";
	public static final Identifier RELOAD_RENDERER = new Identifier("reload_renderer");
	public static final Identifier NEXT_SEASON = new Identifier("next_season");

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

		ServerTickEvents.END_WORLD_TICK.register((world)->{
			short currentTime = (short) (world.getTimeOfDay() % 24000L);//getting time of day

			if (currentTime == 0){
				current_day += 1;

				if (current_day >= MAX_DAY_COUNT){
					this.nextSeason(world);
				}

			}
		});
	}

	void nextSeason(ServerWorld world){
		current_season = current_season.next();

		for (ServerPlayerEntity player : world.getPlayers()){
			ServerPlayNetworking.send(player, NEXT_SEASON, PacketByteBufs.empty());
		}

		current_day = 1;
	}

	static void load(){
		JsonElement element;
		try{
			JsonObject object = SimpleJSON.loadFrom(config_path);
			element = object.get("current_day");

			if(element != null && element.isJsonPrimitive()){
				current_day = element.getAsJsonPrimitive().getAsByte();
				LOGGER.info("Loaded {} as the current day", current_day);
			}

			element = object.get("current_season");

			if(element != null && element.isJsonPrimitive()){
				current_season = Seasons.getSeason(element.getAsJsonPrimitive().getAsString());
				LOGGER.info("Loaded {} as the current season", current_season);
			}

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
			LOGGER.error(e.toString());
		}
	}

	static void save() {
		JsonObject attributes = new JsonObject();
		attributes.addProperty("current_day", current_day);
		attributes.addProperty("current_season", current_season.toString());
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