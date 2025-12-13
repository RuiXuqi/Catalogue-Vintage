package com.mrcrayfish.catalogue.client;

import com.mojang.blaze3d.platform.NativeImage;
import com.mrcrayfish.catalogue.Constants;
import com.mrcrayfish.catalogue.Utils;
import com.mrcrayfish.catalogue.exception.InvalidBrandingImageException;
import com.mrcrayfish.catalogue.exception.ModResourceNotFoundException;
import com.mrcrayfish.catalogue.platform.ClientServices;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Function;

/**
 * Author: MrCrayfish
 */
public record Branding(String prefix, int imageWidth, int imageHeight,
                       BiPredicate<NativeImage, Branding> predicate,
                       Function<IModData, String> locator, boolean override)
{
    public static final Branding ICON = new Branding("icon", 256, 256, ImagePredicate.SQUARE.and(ImagePredicate.LESS_THAN_OR_EQUAL), IModData::getImageIcon, false);
    public static final Branding BANNER = new Branding("banner", 1280, 256, ImagePredicate.LESS_THAN_OR_EQUAL, IModData::getBanner, false);
    public static final Branding BACKGROUND = new Branding("background", 512, 256, ImagePredicate.EQUAL, IModData::getBackground, true);

    public Optional<ImageInfo> loadResource(IModData data)
    {
        String resource = this.locator.apply(data);
        if(resource == null || resource.isBlank())
            return Optional.empty();

        String modId = data.getModId();
        NativeImage image = null;
        try
        {
            image = ClientServices.PLATFORM.loadImageFromModResource(modId, resource);
            this.predicate.test(image, this); // An InvalidBrandingImageException will be thrown if anything is wrong
            DynamicTexture texture = new DynamicTexture(() -> this.prefix, image); // TODO test
            Identifier id = this.override ? Utils.resource(this.prefix) :
                Utils.resource("%s/%s".formatted(this.prefix, data.getModId()));
            Minecraft.getInstance().getTextureManager().register(id, texture);
            return Optional.of(new ImageInfo(id, image.getWidth(), image.getHeight(), () -> {
                Minecraft.getInstance().getTextureManager().release(id);
            }));
        }
        catch(InvalidBrandingImageException e)
        {
            Constants.LOG.error("Invalid {} branding resource '{}' for mod '{}'", this.prefix, resource, modId, e);
        }
        catch(ModResourceNotFoundException e)
        {
            Constants.LOG.error("Unable to locate the {} branding resource '{}' for mod '{}'", this.prefix, resource, modId, e);
        }
        catch(IOException e)
        {
            Constants.LOG.error("An error occurred when loading the {} branding resource '{}' for mod '{}'", this.prefix, resource, modId, e);
        }

        // Free resource if possible since this is only reached if error
        if(image != null)
        {
            image.close();
        }

        return Optional.empty();
    }
}
