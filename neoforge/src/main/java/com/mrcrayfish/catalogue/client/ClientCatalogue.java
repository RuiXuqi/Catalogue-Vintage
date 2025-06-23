package com.mrcrayfish.catalogue.client;

import com.mrcrayfish.catalogue.Constants;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLPaths;

/**
 * Author: MrCrayfish
 */
@EventBusSubscriber(modid = Constants.MOD_ID, value = Dist.CLIENT)
public class ClientCatalogue
{
    @SubscribeEvent
    private static void onClientSetup(FMLClientSetupEvent event)
    {
        Config.load(FMLPaths.CONFIGDIR.get());
    }
}
