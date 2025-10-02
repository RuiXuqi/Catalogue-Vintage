package com.cleanroommc.catalogue;

import net.minecraft.util.ResourceLocation;

/**
 * Author: MrCrayfish
 */
public class Utils {
    public static ResourceLocation resource(String name) {
        return new ResourceLocation(CatalogueConstants.MOD_ID, name);
    }
}
