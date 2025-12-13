package com.mrcrayfish.catalogue;

import net.minecraft.resources.Identifier;

/**
 * Author: MrCrayfish
 */
public class Utils
{
    public static Identifier resource(String name)
    {
        return Identifier.fromNamespaceAndPath(Constants.MOD_ID, name);
    }
}
