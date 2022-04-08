package openseasons.mixin;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import openseasons.OpenSeasonsMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public abstract class WorldMixin {

    @Inject(method = "hasRain(Lnet/minecraft/util/math/BlockPos;)Z", at = @At("TAIL"), cancellable = true)
    private void hasRainInject(BlockPos pos, CallbackInfoReturnable<Boolean> cir){
        cir.setReturnValue(OpenSeasonsMod.currentSeason.getPrecipitation() == Biome.Precipitation.RAIN);

    }
}
