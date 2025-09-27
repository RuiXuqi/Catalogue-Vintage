package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.CatalogueConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.IModGuiFactory;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.ArtifactVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ForgeModData implements IModData {
    public static final ResourceLocation VERSION_CHECK_ICONS = new ResourceLocation("forge", "textures/gui/version_check_icons.png");
    public static final List<String> LIB_MODS = Arrays.asList(CatalogueConfig.libraryList);

    private final ModContainer info;
    private final Type type;
    private final Set<String> dependencies;

    public ForgeModData(ModContainer info) {
        this.info = info;
        this.type = analyzeType(info);
        this.dependencies = this.analyzeDependencies(info);
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getModId() {
        return this.info.getModId();
    }

    @Override
    public String getDisplayName() {
        return this.info.getName();
    }

    @Override
    public @Nonnull String getVersion() {
        return this.info.getDisplayVersion();
    }

    @Override
    public @Nonnull String getInnerVersion() {
        return this.info.getVersion();
    }

    @Override
    public @Nonnull String getDescription() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.description : "";
    }

    @Override
    public @Nonnull String getItemIcon() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.iconItem : "";
        return "";
    }

    @Override
    public @Nonnull String getImageIcon() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.iconFile : "";
        return "";
    }

    @Override
    public @Nonnull String getLicense() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.license : "";
        return "";
    }

    @Override
    public @Nonnull String getCredits() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.credits : "";
    }

    @Override
    public @Nonnull String getAuthors() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.getAuthorList() : "";
    }

    @Override
    public @Nonnull String getHomepage() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.url : "";
    }

    @Override
    public @Nonnull String getIssueTracker() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.issueTrackerUrl : "";
        return "";
    }

    @Override
    public @Nonnull String getBanner() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.logoFile : "";
    }

    @Override
    public @Nonnull String getBackground() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.backgroundFile : "";
        return "";
    }

    @Override
    public @Nullable Update getUpdate() {
        ForgeVersion.CheckResult result = ForgeVersion.getCleanResult(this.info);
        if (result != null && result.status.shouldDraw()) {
            return new Update(result.status.isAnimated(), result.url, result.status.getSheetOffset(), VERSION_CHECK_ICONS, result.status == ForgeVersion.Status.OUTDATED || result.status == ForgeVersion.Status.BETA_OUTDATED, result.latestFound, result.homepage);
        }
        return null;
    }

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public boolean hasConfig() {
        if (this.info == null) return false;
        IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
        if (guiFactory == null) return false;
        return guiFactory.hasConfigGui();
    }

    @Override
    public boolean isLogoSmooth() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.logoBlur : true;
        return true;
    }

    @Override
    public boolean isIconSmooth() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.iconBlur : true;
        return true;
    }

    @Override
    public boolean isLibrary() {
        return this.info.getModId().equals("forge") || this.type != Type.DEFAULT;
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        try {
            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
            GuiScreen newScreen = guiFactory.createConfigGui(parent);
            minecraft.displayGuiScreen(newScreen);
        } catch (Exception e) {
            CatalogueConstants.LOG.error("There was a critical issue trying to build the config GUI for {}", this.getModId(), e);
        }
    }

    @Override
    public void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        int vOffset = update.animated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
        minecraft.getTextureManager().bindTexture(update.textures());
        Gui.drawModalRectWithCustomSizedTexture(x, y, update.texOffset() * 8, vOffset, 8, 8, 64, 16);
    }

    @Override
    public @Nonnull String getUpdateText(Update update) {
        ForgeVersion.CheckResult result = ForgeVersion.getCleanResult(this.info);
        if (result == null) return "";
        switch (result.status) {
            case BETA:
                return TextFormatting.GOLD + I18n.format("catalogue.gui.beta");
            case AHEAD:
                return TextFormatting.LIGHT_PURPLE + I18n.format("catalogue.gui.ahead", update.latestFound());
            case BETA_OUTDATED:
                if (update.homepage() != null) {
                    return TextFormatting.GOLD + I18n.format("catalogue.gui.beta_update_available", update.latestFound(), update.homepage());
                } else {
                    return TextFormatting.GOLD + I18n.format("catalogue.gui.beta_update_available_no_page", update.latestFound());
                }
            case OUTDATED:
                if (update.homepage() != null) {
                    return TextFormatting.GREEN + I18n.format("catalogue.gui.update_available", update.latestFound(), update.homepage());
                } else {
                    return TextFormatting.GREEN + I18n.format("catalogue.gui.update_available_no_page", update.latestFound());
                }
        }
        return "";
    }

    @Override
    public @Nullable IResourcePack getResourcePack() {
        return FMLClientHandler.instance().getResourcePackFor(this.getModId());
    }

    private @Nullable ModMetadata getMetadata() {
        ModMetadata metadata = this.info.getMetadata();
        return metadata != null && !metadata.autogenerated ? metadata : null;
    }

    private Type analyzeType(ModContainer info) {
        if (LIB_MODS.contains(info.getModId())) {
            return Type.LIBRARY;
        } else {
            return Type.DEFAULT;
        }
    }

    private Set<String> analyzeDependencies(ModContainer source) {
        List<? extends ArtifactVersion> versions = source.getDependencies();
        return versions.stream()
                .map(ArtifactVersion::getLabel)
                .filter(modid -> !modid.equals("minecraft") && !modid.equals("forge"))
                .collect(Collectors.toUnmodifiableSet());
    }
}
