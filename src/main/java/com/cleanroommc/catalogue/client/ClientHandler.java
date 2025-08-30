package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = "catalogue", value = Side.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void onOpenScreen(GuiOpenEvent event) {
        if (event.getGui() instanceof GuiModList) {
            event.setGui(new CatalogueModListScreen());
        }
    }

}
