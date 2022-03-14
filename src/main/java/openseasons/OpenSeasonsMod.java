package openseasons;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.util.ActionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;


public class OpenSeasonsMod implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger("openseasons");

	private byte current_day = 1;
	private final byte MAX_DAY_COUNT = 30;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		ServerTickEvents.END_WORLD_TICK.register((world)->{

			short currentTime = (short) (world.getTimeOfDay() % 24000L);//getting time of day

			if (currentTime == 0){
				this.current_day += 1;

				if (this.current_day >= MAX_DAY_COUNT){
					this.nextSeason();
				}
			}
		});
	}

	private void nextSeason(){
		//this function should set the next season in the following order:
		//...->Summer->Fall->Winter->Spring->...



		this.current_day = 1;
	}
}
