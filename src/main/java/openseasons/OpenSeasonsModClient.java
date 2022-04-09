package openseasons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import openseasons.util.Keys;
import openseasons.util.OpenSeasonsUtil;

@Environment(EnvType.CLIENT)
public class OpenSeasonsModClient extends OpenSeasonsMod implements ClientModInitializer {


    @Override
    public void onInitializeClient() {
        OpenSeasonsMod.LOGGER.info(Keys.MOD_ID+ ":Initializing Client...");

        //register packet receiver
        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.SET_SEASON, (client, handler, buf,
                                                                                responseSender) -> {
            Seasons season = Seasons.getSeason(buf.readString());

            client.execute(()->{
                OpenSeasonsUtil.setSeasonBlocks(season);
                client.worldRenderer.reload();
            });

        });

        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.RELOAD_RENDERER, (client, handler, buf,
                                                                                     responseSender) -> {
            client.execute(()->{
                client.worldRenderer.reload();
            });

        });

        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.UPDATE_BLOCK_STATE, (client, handler, buf, responseSender) -> {

            BlockUpdateS2CPacket blockUpdatePacket = new BlockUpdateS2CPacket(buf);

            client.execute(()->{
                handler.onBlockUpdate(blockUpdatePacket);
            });

        });

    }
}
