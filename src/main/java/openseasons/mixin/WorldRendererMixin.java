package openseasons.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;
import openseasons.OpenSeasonsMod;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow @Final private MinecraftClient client;

    @Shadow private int ticks;

    @Shadow @Final private float[] field_20794;

    @Shadow @Final private float[] field_20795;

    @Shadow @Final private static Identifier RAIN;

    @Shadow @Final private static Identifier SNOW;


    @Inject(method = "renderWeather", at = @At("HEAD"), cancellable = true)
    private void renderWeatherRedirect(LightmapTextureManager manager, float f, double d, double e, double g, CallbackInfo ci) {
        if(OpenSeasonsMod.enableDynamicWeather){
            float h = this.client.world.getRainGradient(f);
            if (h <= 0.0f) {
                return;
            }
            manager.enable();
            ClientWorld world = this.client.world;
            int i = MathHelper.floor(d);
            int j = MathHelper.floor(e);
            int k = MathHelper.floor(g);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferBuilder = tessellator.getBuffer();
            RenderSystem.disableCull();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (MinecraftClient.isFancyGraphicsOrBetter()) {
                l = 10;
            }
            RenderSystem.depthMask(MinecraftClient.isFabulousGraphicsOrBetter());
            int m = -1;
            float n = (float) this.ticks + f;
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            BlockPos.Mutable mutable = new BlockPos.Mutable();
            for (int o = k - l; o <= k + l; ++o) {
                for (int p = i - l; p <= i + l; ++p) {
                    float ac;
                    float y;
                    int w;
                    int q = (o - k + 16) * 32 + p - i + 16;
                    double r = (double) this.field_20794[q] * 0.5;
                    double s = (double) this.field_20795[q] * 0.5;
                    mutable.set((double) p, e, (double) o);
                    Biome biome = world.getBiome(mutable).value();
                    if (biome.getPrecipitation() == Biome.Precipitation.NONE) continue;
                    int t = world.getTopY(Heightmap.Type.MOTION_BLOCKING, p, o);
                    int u = j - l;
                    int v = j + l;
                    if (u < t) {
                        u = t;
                    }
                    if (v < t) {
                        v = t;
                    }
                    if ((w = t) < j) {
                        w = j;
                    }
                    if (u == v) continue;
                    Random random = new Random(p * p * 3121 + p * 45238971 ^ o * o * 418711 + o * 13761);
                    mutable.set(p, u, o);
                    if (OpenSeasonsMod.currentSeason.getPrecipitation() == Biome.Precipitation.RAIN) {
                        if (m != 0) {
                            if (m >= 0) {
                                tessellator.draw();
                            }
                            m = 0;
                            RenderSystem.setShaderTexture(0, this.RAIN);
                            bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                        }
                        int x = this.ticks + p * p * 3121 + p * 45238971 + o * o * 418711 + o * 13761 & 0x1F;
                        y = -((float) x + f) / 32.0f * (3.0f + random.nextFloat());
                        double z = (double) p + 0.5 - d;
                        double aa = (double) o + 0.5 - g;
                        float ab = (float) Math.sqrt(z * z + aa * aa) / (float) l;
                        ac = ((1.0f - ab * ab) * 0.5f + 0.5f) * h;
                        mutable.set(p, w, o);
                        int ad = WorldRenderer.getLightmapCoordinates(world, mutable);
                        bufferBuilder.vertex((double) p - d - r + 0.5, (double) v - e, (double) o - g - s + 0.5).texture(0.0f, (float) u * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).light(ad).next();
                        bufferBuilder.vertex((double) p - d + r + 0.5, (double) v - e, (double) o - g + s + 0.5).texture(1.0f, (float) u * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).light(ad).next();
                        bufferBuilder.vertex((double) p - d + r + 0.5, (double) u - e, (double) o - g + s + 0.5).texture(1.0f, (float) v * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).light(ad).next();
                        bufferBuilder.vertex((double) p - d - r + 0.5, (double) u - e, (double) o - g - s + 0.5).texture(0.0f, (float) v * 0.25f + y).color(1.0f, 1.0f, 1.0f, ac).light(ad).next();
                        continue;
                    }
                    if (m != 1) {
                        if (m >= 0) {
                            tessellator.draw();
                        }
                        m = 1;
                        RenderSystem.setShaderTexture(0, this.SNOW);
                        bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
                    }
                    float ae = -((float) (this.ticks & 0x1FF) + f) / 512.0f;
                    y = (float) (random.nextDouble() + (double) n * 0.01 * (double) ((float) random.nextGaussian()));
                    float af = (float) (random.nextDouble() + (double) (n * (float) random.nextGaussian()) * 0.001);
                    double ag = (double) p + 0.5 - d;
                    double ah = (double) o + 0.5 - g;
                    ac = (float) Math.sqrt(ag * ag + ah * ah) / (float) l;
                    float ai = ((1.0f - ac * ac) * 0.3f + 0.5f) * h;
                    mutable.set(p, w, o);
                    int aj = WorldRenderer.getLightmapCoordinates(world, mutable);
                    int ak = aj >> 16 & 0xFFFF;
                    int al = aj & 0xFFFF;
                    int am = (ak * 3 + 240) / 4;
                    int an = (al * 3 + 240) / 4;
                    bufferBuilder.vertex((double) p - d - r + 0.5, (double) v - e, (double) o - g - s + 0.5).texture(0.0f + y, (float) u * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).light(an, am).next();
                    bufferBuilder.vertex((double) p - d + r + 0.5, (double) v - e, (double) o - g + s + 0.5).texture(1.0f + y, (float) u * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).light(an, am).next();
                    bufferBuilder.vertex((double) p - d + r + 0.5, (double) u - e, (double) o - g + s + 0.5).texture(1.0f + y, (float) v * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).light(an, am).next();
                    bufferBuilder.vertex((double) p - d - r + 0.5, (double) u - e, (double) o - g - s + 0.5).texture(0.0f + y, (float) v * 0.25f + ae + af).color(1.0f, 1.0f, 1.0f, ai).light(an, am).next();
                }
            }
            if (m >= 0) {
                tessellator.draw();
            }
            RenderSystem.enableCull();
            RenderSystem.disableBlend();
            manager.disable();
            ci.cancel();
        }
    }

}
