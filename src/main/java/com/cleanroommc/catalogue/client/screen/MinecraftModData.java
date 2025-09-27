package com.cleanroommc.catalogue.client.screen;

import com.cleanroommc.catalogue.client.IModData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.IResourcePack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;

/**
 * Author: MrCrayfish
 */
public class MinecraftModData implements IModData {
    @Override
    public Type getType() {
        return Type.LIBRARY;
    }

    @Override
    public String getModId() {
        return "minecraft";
    }

    @Override
    public String getDisplayName() {
        return "Minecraft";
    }

    @Override
    public @Nonnull String getVersion() {
        return "1.12.2";
    }

    @Override
    public @Nonnull String getInnerVersion() {
        return this.getVersion();
    }

    @Override
    public @Nonnull String getDescription() {
        // Description provided by minecraft.wiki (CC BY-NC-SA 3.0)
        return "Minecraft is a 3D sandbox adventure game developed by Mojang Studios where players can interact with a fully customizable three-dimensional world made of blocks and entities. Its diverse gameplay options allow players to choose the way they play, creating countless possibilities.";
    }

    @Override
    public @Nonnull String getItemIcon() {
        return "";
    }

    @Override
    public @Nonnull String getImageIcon() {
        return "";
    }

    @Override
    public @Nonnull String getLicense() {
        return "All Rights Reserved";
    }

    @Override
    public @Nonnull String getCredits() {
        return "";
    }

    @Override
    public @Nonnull String getAuthors() {
        return "Mojang AB";
    }

    @Override
    public @Nonnull String getHomepage() {
        return "https://www.minecraft.net";
    }

    @Override
    public @Nonnull String getIssueTracker() {
        return "https://bugs.mojang.com/projects/MC/issues";
    }

    @Override
    public @Nonnull String getBanner() {
        return "";
    }

    @Override
    public @Nonnull String getBackground() {
        return "";
    }

    @Override
    public @Nullable Update getUpdate() {
        return null;
    }

    @Override
    public @Nullable IResourcePack getResourcePack() {
        return null;
    }

    @Override
    public Set<String> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public boolean hasConfig() {
        return true;
    }

    @Override
    public boolean isLogoSmooth() {
        return true;
    }

    @Override
    public boolean isIconSmooth() {
        return true;
    }

    @Override
    public boolean isLibrary() {
        return true;
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        minecraft.displayGuiScreen(new GuiOptions(parent, minecraft.gameSettings));
    }

    @Override
    public void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y) {

    }

    @Override
    public @Nonnull String getUpdateText(Update update) {
        return "";
    }
}
