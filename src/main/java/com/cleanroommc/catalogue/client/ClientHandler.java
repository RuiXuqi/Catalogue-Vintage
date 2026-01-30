package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = CatalogueConstants.MOD_ID, value = Side.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void onOpenScreen(@NotNull GuiOpenEvent event) {
        if (!CatalogueConfig.enable) return;
        if (event.getGui() instanceof GuiModList screen) {
            GuiScreen parent;
            try {
                Field field = GuiModList.class.getDeclaredField("mainMenu");
                field.setAccessible(true);
                parent = (GuiScreen) field.get(screen);
            } catch (Exception e) {
                CatalogueConstants.LOG.error("Failed to get field mainMenu from GuiModList", e);
                parent = Minecraft.getMinecraft().currentScreen;
            }
            event.setGui(new CatalogueModListScreen(parent));
        }
    }
}
