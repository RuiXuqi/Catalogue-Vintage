package com.cleanroommc.catalogue.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public interface IModData {
    Type getType();

    String getModId();

    String getDisplayName();

    @Nonnull String getVersion();

    @Nonnull String getInnerVersion();

    @Nonnull String getDescription();

    @Nonnull String getItemIcon();

    @Nonnull String getImageIcon();

    @Nonnull String getLicense();

    @Nonnull String getCredits();

    @Nonnull String getAuthors();

    @Nonnull String getHomepage();

    @Nonnull String getIssueTracker();

    @Nonnull String getBanner();

    @Nonnull String getBackground();

    @Nullable Update getUpdate();

    @Nullable IResourcePack getResourcePack();

    Set<String> getDependencies(); //TODO lazily

    boolean hasConfig();

    boolean isLogoSmooth();

    boolean isIconSmooth();

    boolean isLibrary();

    void openConfigScreen(Minecraft minecraft, GuiScreen parent);

    void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y);

    @Nonnull String getUpdateText(Update update);

    record Update(boolean animated, String url, int texOffset, ResourceLocation textures, boolean updatable, String latestFound, String homepage) {
    }

    enum Type {
        DEFAULT(TextFormatting.RESET),
        LIBRARY(TextFormatting.DARK_GRAY),
        GENERATED(TextFormatting.AQUA);

        private final TextFormatting style;

        Type(TextFormatting style) {
            this.style = style;
        }

        public TextFormatting getStyle() {
            return this.style;
        }
    }
}
