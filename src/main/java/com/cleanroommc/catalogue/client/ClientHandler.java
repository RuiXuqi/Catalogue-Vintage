package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import cpw.mods.fml.client.GuiIngameModOptions;
import cpw.mods.fml.client.GuiModList;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
public class ClientHandler {
    @SubscribeEvent
    public void onOpenScreen(@Nonnull GuiOpenEvent event) {
        if (!CatalogueConfig.enable) return;
        if (event.gui instanceof GuiModList screen) {
            GuiScreen parent;
            try {
                Field field = GuiModList.class.getDeclaredField("mainMenu");
                field.setAccessible(true);
                parent = (GuiScreen) field.get(screen);
            } catch (Exception e) {
                CatalogueConstants.LOG.error("Failed to get field mainMenu from GuiModList", e);
                parent = Minecraft.getMinecraft().currentScreen;
            }
            event.gui = new CatalogueModListScreen(parent);
            return;
        }
        if (event.gui instanceof GuiIngameModOptions screen) {
            GuiScreen parent;
            try {
                Field field = GuiIngameModOptions.class.getDeclaredField("parentScreen");
                field.setAccessible(true);
                parent = (GuiScreen) field.get(screen);
            } catch (Exception e) {
                CatalogueConstants.LOG.error("Failed to get field parentScreen from GuiIngameModOptions", e);
                parent = Minecraft.getMinecraft().currentScreen;
            }
            event.gui = new CatalogueModListScreen(parent);
        }
    }
}
