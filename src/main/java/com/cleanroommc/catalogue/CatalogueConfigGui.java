package com.cleanroommc.catalogue;

import cpw.mods.fml.client.config.GuiConfig;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

public class CatalogueConfigGui extends GuiConfig {
    public CatalogueConfigGui(GuiScreen parent) {
        //noinspection unchecked,rawtypes
        super(
            parent,
            new ConfigElement(CatalogueConfig.getConfig().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
            CatalogueConstants.MOD_ID,
            false, false,
            CatalogueConstants.MOD_NAME
        );
    }
}
