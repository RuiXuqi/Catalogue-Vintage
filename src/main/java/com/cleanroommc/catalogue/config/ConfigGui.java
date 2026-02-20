package com.cleanroommc.catalogue.config;

import com.cleanroommc.catalogue.CatalogueConstants;
import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;

public class ConfigGui extends GuiConfig {
    public ConfigGui(GuiScreen parent) {
        super(
                parent,
                Config.getRootConfigElements(),
                CatalogueConstants.MOD_ID,
                false, false,
                CatalogueConstants.MOD_NAME
        );
    }
}
