package openseasons;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import openseasons.util.Keys;

import java.security.Key;

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
     * @param world
     * @return
     */
    public static OpenSeasonsWorldState getState(ServerWorld world){
        OpenSeasonsWorldState state = new OpenSeasonsWorldState();

        state = world.getPersistentStateManager().get(nbtCompound -> {

            byte day = nbtCompound.getByte(Keys.CURRENT_DAY);
            Seasons season = Seasons.getSeason(nbtCompound.getString(Keys.CURRENT_SEASON));
            return new OpenSeasonsWorldState(day, season);

        }, Keys.WORLD_STATE);

        if (state == null){
            OpenSeasonsMod.LOGGER.warn("Null state when trying to read");
            OpenSeasonsMod.LOGGER.info("Assuming default values");
            state = new OpenSeasonsWorldState( (byte) 1, Seasons.SUMMER);
        }

        if (!state.isInitialized()){
            OpenSeasonsMod.LOGGER.warn("Uninitialized state when trying to read");
            OpenSeasonsMod.LOGGER.info("Assuming default values");
            state = new OpenSeasonsWorldState( (byte) 1, Seasons.SUMMER);
        }

        return state;
    }

}