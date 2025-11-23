package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;

import javax.annotation.Nonnull;

/**
 * Author: MrCrayfish
 */
public class ClientHandler {
    @SubscribeEvent
    public void onOpenScreen(@Nonnull GuiOpenEvent event) {
        if (CatalogueConfig.enableMod && (event.gui instanceof GuiModList || event.gui instanceof GuiIngameModOptions)) {
            event.gui = new CatalogueModListScreen(Minecraft.getMinecraft().currentScreen);
        }
    }
}
