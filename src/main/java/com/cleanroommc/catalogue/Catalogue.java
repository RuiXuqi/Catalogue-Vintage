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
@Mod(modid = CatalogueConstants.MOD_ID, name = CatalogueConstants.MOD_NAME, version = Reference.VERSION, acceptableRemoteVersions = "*")
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
