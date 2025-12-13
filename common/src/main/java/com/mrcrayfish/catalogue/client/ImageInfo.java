package com.mrcrayfish.catalogue.client;

import net.minecraft.resources.Identifier;

/**
 * Author: MrCrayfish
 */
public record ImageInfo(Identifier resource, int width, int height, Runnable unregister)
{
}
