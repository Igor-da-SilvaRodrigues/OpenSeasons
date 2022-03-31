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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.WorldChunk;
import openseasons.JSON.SimpleJSON;
import openseasons.commands.DayCommand;
import openseasons.commands.OpenseasonsCommand;
import openseasons.commands.SeasonCommand;
import openseasons.util.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Random;

import static net.minecraft.state.property.Properties.SNOWY;

public class OpenSeasonsMod implements ModInitializer {
	public static final String MOD_ID = Keys.MOD_ID;
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static byte MAX_DAY_COUNT = 10;
	private static final String config_path = "config/openseasons.json";

	public static final Identifier RELOAD_RENDERER = new Identifier(MOD_ID,Keys.RELOAD_RENDERER);
	public static final Identifier NEXT_SEASON = new Identifier(MOD_ID,Keys.NEXT_SEASON);
	public static final Identifier CLIENT_JOIN = new Identifier(MOD_ID, Keys.CLIENT_JOIN);
	public static final Identifier RELOAD_BLOCK_STATE = new Identifier(MOD_ID, Keys.RELOAD_BLOCK_STATE);

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

			//--------------
			Random random = new Random();

			OpenSeasonsWorldState worldState = OpenSeasonsWorldState.getState(world);
			if (worldState.current_season.getTemperature() > 0.1f && random.nextInt(10) == 1){//10% chance of a chunk being allowed to melt at this tick.
				meltBlocks(world);
			}


		});//tick

		registerCommands();

	}

	/**
	 * Notifies all clients to reload their world renderer. Needs to be called for a season to render.
	 * @param world The target world
	 * @param worldState The state containing the desired season.
	 */
	static void reloadSeason(ServerWorld world, OpenSeasonsWorldState worldState){

		PacketByteBuf buffer = PacketByteBufs.create();
		buffer.writeString(worldState.current_season.toString());

		for (ServerPlayerEntity player : world.getPlayers()){
			ServerPlayNetworking.send(player, NEXT_SEASON, buffer);
		}

	}
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
	 *@see OpenSeasonsMod#updateClientBlock(BlockPos, BlockState, ServerPlayerEntity)
	 */
	private static void meltBlocks(ServerWorld world){
		Random random = new Random();
		for (ServerPlayerEntity player : world.getPlayers()){
			WorldChunk chunk = world.getWorldChunk(player.getBlockPos()); // chunk where the player stands;

			int bottom = chunk.getBottomY();
			int top = chunk.getTopY();
			if (top > 100) {
				top = 100;
			}
			if (bottom < 40) {
				bottom = 40;
			}

			for (int x = 0; x < 16; x += 1){
				for (int y = bottom ;y <= top ; y += 1){//very high mountains will get to keep their snow cover in all
					// seasons.
					for (int z = 0; z < 16; z += 1){
						if (random.nextInt(10) == 1){// second layer of randomness, this will make it so not all blocks will melt at the same time.
							//LOGGER.info("Checking position x:{}, y:{}, z:{}", x,y,z);
							BlockPos blockPos = new BlockPos(x,y,z);
							BlockState blockState = chunk.getBlockState(blockPos);

							if (blockState == Blocks.SNOW.getDefaultState() || blockState == Blocks.SNOW_BLOCK.getDefaultState()){
								LOGGER.info("Found snow");
								chunk.setBlockState(blockPos, Blocks.AIR.getDefaultState(), false);

								updateClientBlock( blockPos, Blocks.AIR.getDefaultState(), player);
							}

							if (blockState == Blocks.GRASS_BLOCK.getDefaultState().with(SNOWY, true)){
								LOGGER.info("Found snowy grass");
								chunk.setBlockState(blockPos, blockState.with(SNOWY, false), false);

								updateClientBlock( blockPos, blockState.with(SNOWY, false), player);
							}

						}
					}
				}
			}

		}
	}



	/**
	 * Notifies a client to update a Block State.
	 * @param blockPos The target block position in the target chunk
	 * @param blockState The desired new block state
	 * @param player The client that will receive the update.
	 */
	private static void updateClientBlock(BlockPos blockPos, BlockState blockState, ServerPlayerEntity player){
		BlockUpdateS2CPacket blockUpdatePacket = new BlockUpdateS2CPacket(blockPos, blockState);

		PacketByteBuf buf = PacketByteBufs.create();
		blockUpdatePacket.write(buf);
		buf.writeChunkPos(player.getChunkPos());

		ServerPlayNetworking.send(player, RELOAD_BLOCK_STATE, buf);
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
				LOGGER.info(MOD_ID +":Loaded {} as the current day limit",MAX_DAY_COUNT);
			}

			element = object.get("summer_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.SUMMER.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Summer color",color);
			}

			element = object.get("fall_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.FALL.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Fall color",color);
			}

			element = object.get("winter_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.WINTER.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Winter color",color);
			}

			element = object.get(":spring_color");
			if(element != null && element.isJsonPrimitive()) {
				String color = element.getAsJsonPrimitive().getAsString();
				Seasons.SPRING.setFoliagecolor(color);
				LOGGER.info(MOD_ID +":Loaded {} as the Spring color",color);
			}
		}catch (IOException e){
			LOGGER.error(MOD_ID +":Failed to load configuration\n"+ e);
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
			LOGGER.error(MOD_ID +":Failed to save configuration\n"+e);
		}
	}
}