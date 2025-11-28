package com.cleanroommc.catalogue;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nonnull;

public class CatalogueConfigHandler {
    @SubscribeEvent
    public void onConfigChanged(@Nonnull ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.modID.equals(CatalogueConstants.MOD_ID)) {
            CatalogueConfig.syncConfig();
        }
    }
}
