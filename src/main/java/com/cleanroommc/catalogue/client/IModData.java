package com.cleanroommc.catalogue.client;

import com.github.bsideup.jabel.Desugar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public interface IModData {
    Type getType();

    String getModId();

    String getDisplayName();

    String getVersion();

    String getInnerVersion();

    @Nullable
    String getDescription();

    @Nullable
    String getItemIcon();

    @Nullable
    String getImageIcon();

    @Nullable
    String getLicense();

    @Nullable
    String getCredits();

    @Nullable
    String getAuthors();

    @Nullable
    String getHomepage();

    @Nullable
    String getIssueTracker();

    @Nullable
    String getBanner();

    @Nullable
    String getBackground();

    @Nullable
    String getChildMods();

    @Nullable
    Update getUpdate();

    @Nullable
    IResourcePack getResourcePack();

    Set<String> getDependencies(); //TODO lazily

    boolean hasConfig();

    boolean isLibrary();

    void openConfigScreen(Minecraft minecraft, GuiScreen parent);

    void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y);

    @Nullable
    String getUpdateText(Update update);

    @Desugar
    record Update(boolean animated, String url, int texOffset, ResourceLocation textures, boolean updatable,
                  String latestFound, String homepage) {
    }

    enum Type {
        DEFAULT(EnumChatFormatting.RESET),
        LIBRARY(EnumChatFormatting.DARK_GRAY),
        GENERATED(EnumChatFormatting.AQUA);

        private final EnumChatFormatting style;

        Type(EnumChatFormatting style) {
            this.style = style;
        }

        public EnumChatFormatting getStyle() {
            return this.style;
        }
    }
}
