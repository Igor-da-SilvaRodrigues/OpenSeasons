package openseasons;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import openseasons.util.Keys;
import openseasons.util.S2C;

public class OpenSeasonsWorldState extends PersistentState {

    public byte current_day;
    public Seasons current_season;

    public OpenSeasonsWorldState(byte day, Seasons season){
        super();
        current_day = day;
        current_season = season;
    }


    public OpenSeasonsWorldState(){
        super();
        current_day = 0;
        current_season = null;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        nbt.putByte(Keys.CURRENT_DAY, current_day);
        nbt.putString(Keys.CURRENT_SEASON, current_season.toString());
        return nbt;
    }

    public boolean isInitialized(){
        return (current_day != 0 && current_season != null);
    }

    /**
     * Gets the OpenSeasonsWorldState from a ServerWorld and returns it. If it fails to load a state for any reason, a
     * default state will be returned instead.
     * @param world The target world
     * @return The world state
     */
    public static OpenSeasonsWorldState getState(ServerWorld world){
        OpenSeasonsWorldState state = new OpenSeasonsWorldState();

        state = world.getPersistentStateManager().get(nbtCompound -> {

            byte day = nbtCompound.getByte(Keys.CURRENT_DAY);
            Seasons season = Seasons.getSeason(nbtCompound.getString(Keys.CURRENT_SEASON));
            return new OpenSeasonsWorldState(day, season);

        }, Keys.WORLD_STATE);

        if (state == null){
            OpenSeasonsMod.LOGGER.warn(Keys.MOD_ID +":Null state when trying to read");
            OpenSeasonsMod.LOGGER.info(Keys.MOD_ID +":Assuming default values");
            state = new OpenSeasonsWorldState( (byte) 1, Seasons.SUMMER);
        }

        if (!state.isInitialized()){
            OpenSeasonsMod.LOGGER.warn(Keys.MOD_ID +":Uninitialized state when trying to read");
            OpenSeasonsMod.LOGGER.info(Keys.MOD_ID +":Assuming default values");
            state = new OpenSeasonsWorldState( (byte) 1, Seasons.SUMMER);
        }

        return state;
    }

    /**
     * <p>Sets the state to a world. This can also notify clients to reload their world renderer.
     *
     * @param world The target world
     * @param worldState The desired world state
     * @param seasonChanged Will notify clients to reload their world renderer when true.
     */
    public static void setState(ServerWorld world, OpenSeasonsWorldState worldState, boolean seasonChanged){
        if (seasonChanged){
            if(OpenSeasonsMod.enableDynamicWeather){
                OpenSeasonsMod.currentSeason = worldState.current_season;
            }
            S2C.setAllClientsSeason(world, worldState);
        }

        if (!worldState.isDirty()) worldState.markDirty();
        world.getPersistentStateManager().set(Keys.WORLD_STATE, worldState);
    }
}
