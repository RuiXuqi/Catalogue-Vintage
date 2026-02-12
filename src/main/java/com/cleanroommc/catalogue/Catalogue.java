package com.cleanroommc.catalogue;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nonnull;

/**
 * Author: MrCrayfish
 */
@SuppressWarnings("unused")
@Mod(
        modid = CatalogueConstants.MOD_ID,
        name = CatalogueConstants.MOD_NAME,
        version = Reference.VERSION,
        acceptableRemoteVersions = "*",
        guiFactory = "com.cleanroommc.catalogue.CatalogueConfigGuiFactory",
        customProperties = {
                @Mod.CustomProperty(k = "license", v = "MIT"),
                @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/RuiXuqi/Catalogue-Vintage/issues"),
                @Mod.CustomProperty(k = "iconFile", v = "assets/catalogue/icon.png"),
                @Mod.CustomProperty(k = "backgroundFile", v = "assets/catalogue/background.png")
        }
)
public class Catalogue {
    @SidedProxy(clientSide = "com.cleanroommc.catalogue.ClientProxy", serverSide = "com.cleanroommc.catalogue.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(@Nonnull FMLPreInitializationEvent event) {
        proxy.preInit(event);
    }

    @Mod.EventHandler
    public void init(@Nonnull FMLInitializationEvent event) {
        proxy.init(event);
    }
}
