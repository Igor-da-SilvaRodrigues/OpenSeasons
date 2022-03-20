package openseasons;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;


@Environment(EnvType.CLIENT)
public class OpenSeasonsModClient extends OpenSeasonsMod implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OpenSeasonsMod.LOGGER.info("Initializing Client...");

        ClientPlayConnectionEvents.INIT.register((handler, client) -> {
            OpenSeasonsUtil.setSeasonBlocks();

            client.execute(()->{
                client.worldRenderer.reload();
            });
        });

        //register packet receiver
        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.NEXT_SEASON, (client, handler, buf,
                                                                                 responseSender) -> {
            OpenSeasonsUtil.setSeasonBlocks();

           client.execute(()->{
               client.worldRenderer.reload();
           });

        });

        ClientPlayNetworking.registerGlobalReceiver(OpenSeasonsMod.RELOAD_RENDERER, (client, handler, buf,
                                                                                     responseSender) -> {
            client.execute(()->{
                client.worldRenderer.reload();
            });

        });
    }
}
