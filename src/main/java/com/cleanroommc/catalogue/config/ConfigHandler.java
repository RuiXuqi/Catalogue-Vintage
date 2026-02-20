package com.cleanroommc.catalogue.config;

import com.cleanroommc.catalogue.CatalogueConstants;
import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

public class ConfigHandler {
    @SubscribeEvent
    public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(CatalogueConstants.MOD_ID)) {
            Config.readFromProp();
        }
    }
}
