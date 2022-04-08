package openseasons.mixin;

import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.LightType;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BuiltinBiomes;
import openseasons.OpenSeasonsMod;
import openseasons.Seasons;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Biome.class)
public abstract class BiomeMixin {

    @Shadow abstract Biome.Category getCategory();

    //@Shadow @Final private Biome.Weather weather;

    @Shadow public abstract boolean isCold(BlockPos pos);


    @Inject(method = "doesNotSnow(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("TAIL"), cancellable = true)
    private void doesNotSnowInject(BlockPos pos, CallbackInfoReturnable<Boolean> cir ){
        //if it rains it doesn't snow
        if (OpenSeasonsMod.enableDynamicWeather && OpenSeasonsMod.currentSeason.getPrecipitation() == Biome.Precipitation
        .RAIN){
            cir.setReturnValue(true);
        }
    }


    @Inject(method = "canSetSnow(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;)Z", at = @At("HEAD"), cancellable = true)
    private void canSetSnowInject(WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        BlockState blockState1;
        Biome.Category category = getCategory();
        if (category == Biome.Category.ICY || category == Biome.Category.MOUNTAIN || category == Biome.Category.TAIGA){

            cir.setReturnValue(pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, pos) < 10 && (blockState1 = world.getBlockState(pos)).isAir() && Blocks.SNOW.getDefaultState().canPlaceAt(world, pos));

        }
        cir.setReturnValue(false);
    }

    /*
    @Inject(method = "canSetIce(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Z)Z", at = @At("HEAD"), cancellable = true)
    private void canSetIceInject(WorldView world, BlockPos pos, boolean doWaterCheck, CallbackInfoReturnable<Boolean> cir){

        if (OpenSeasonsMod.currentSeason == Seasons.WINTER){
            OpenSeasonsMod.LOGGER.warn("It's winter, may generate ice");

            if (pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY() && world.getLightLevel(LightType.BLOCK, pos) < 10){
                FluidState fluid = world.getFluidState(pos);
                if (fluid.getFluid() == Fluids.WATER){
                    //!doWaterCheck
                    //if (!doWaterCheck){
                        cir.setReturnValue(true);
                   // }
                }else{
                    OpenSeasonsMod.LOGGER.warn("Failed to generate ice");
                    OpenSeasonsMod.LOGGER.warn("because the fluid was not water");
                }


            }else{
                OpenSeasonsMod.LOGGER.warn("Failed to generate ice");
                if (!(pos.getY() >= world.getBottomY() && pos.getY() < world.getTopY())){
                    OpenSeasonsMod.LOGGER.warn("because block was out of generation range");
                }

                if (!(world.getLightLevel(LightType.BLOCK, pos) < 10)){
                    OpenSeasonsMod.LOGGER.warn("because there was too much light.");
                }
            }
        }

        cir.setReturnValue(false);

    }


     */


    //@Inject(method = "getTemperature()F", at = @At("TAIL"), cancellable = true)
    //private void getTemperatureInject(CallbackInfoReturnable<Float> cir){
    //    if (OpenSeasonsMod.enableDynamicWeather){
    //        cir.setReturnValue(OpenSeasonsMod.currentSeason.getTemperature());
    //    }
    //}

    //@Inject(method = "getTemperature(Lnet/minecraft/util/math/BlockPos;)F", at = @At("RETURN"), cancellable = true)
    //private void getTemperatureInject(BlockPos blockpos , CallbackInfoReturnable<Float> cir){
    //   if(OpenSeasonsMod.enableDynamicWeather){
    //        cir.setReturnValue(OpenSeasonsMod.currentSeason.getTemperature());
    //    }
    //}

    @Inject(method = "getPrecipitation()Lnet/minecraft/world/biome/Biome$Precipitation;", at = @At("TAIL"), cancellable = true)
    private void getPrecipitationInject(CallbackInfoReturnable<Biome.Precipitation> cir){
        if(OpenSeasonsMod.enableDynamicWeather){
            if (getCategory() == Biome.Category.DESERT ||
                    getCategory() == Biome.Category.SAVANNA ||
                    getCategory() == Biome.Category.MESA ||
                    getCategory() == Biome.Category.NONE) {
                cir.setReturnValue(Biome.Precipitation.NONE);
            } else {
                cir.setReturnValue(OpenSeasonsMod.currentSeason.getPrecipitation());
            }
        }
    }

}
