package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.client.screen.CatalogueModListScreen;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.client.GuiModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;

/**
 * Author: MrCrayfish
 */
@Mod.EventBusSubscriber(modid = CatalogueConstants.MOD_ID, value = Side.CLIENT)
public class ClientHandler {
    @SubscribeEvent
    public static void onOpenScreen(@NotNull GuiOpenEvent event) {
        if (CatalogueConfig.enableMod && event.getGui() instanceof GuiModList) {
            event.setGui(new CatalogueModListScreen(Minecraft.getMinecraft().currentScreen));
        }
    }
}
