package com.cleanroommc.catalogue.client;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.CatalogueConstants;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.versioning.ArtifactVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.IResourcePack;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ForgeModData implements IModData {
    public static final List<String> LIB_MODS = Arrays.asList(CatalogueConfig.libraryList);
    public static final List<String> IGNORED_DEPENDENCIES = Arrays.asList(CatalogueConfig.ignoredDependenciesList);

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
    public String getVersion() {
        return this.info.getDisplayVersion();
    }

    @Override
    public String getInnerVersion() {
        return this.info.getVersion();
    }

    @Nullable
    @Override
    public String getDescription() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.description : null;
    }

    @Nullable
    @Override
    public String getItemIcon() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.iconItem : null;
        return null;
    }

    @Nullable
    @Override
    public String getImageIcon() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.iconFile : null;
        return null;
    }

    @Nullable
    @Override
    public String getLicense() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.license : null;
        return null;
    }

    @Nullable
    @Override
    public String getCredits() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.credits : null;
    }

    @Nullable
    @Override
    public String getAuthors() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.getAuthorList() : null;
    }

    @Nullable
    @Override
    public String getHomepage() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.url : null;
    }

    @Nullable
    @Override
    public String getIssueTracker() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.issueTrackerUrl : null;
        return null;
    }

    @Nullable
    @Override
    public String getBanner() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.logoFile : null;
    }

    @Nullable
    @Override
    public String getBackground() {
        ModMetadata metadata = this.getMetadata();
        //return metadata != null ? metadata.backgroundFile : null;
        return null;
    }

    @Nullable
    @Override
    public String getChildMods() {
        ModMetadata metadata = this.getMetadata();
        return metadata != null ? metadata.getChildModList() : null;
    }

    @Nullable
    @Override
    public Update getUpdate() {
        return null;
    }

    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Override
    public boolean hasConfig() {
        IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
        return guiFactory != null && guiFactory.mainConfigGuiClass() != null;
    }

    @Override
    public boolean isLibrary() {
        return this.info.getModId().equals("forge") || this.type != Type.DEFAULT;
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        try {
            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
            GuiScreen newScreen = guiFactory.mainConfigGuiClass().getConstructor(GuiScreen.class).newInstance(parent);
            minecraft.displayGuiScreen(newScreen);
        } catch (Exception e) {
            CatalogueConstants.LOG.error("There was a critical issue trying to build the config GUI for {}", this.getModId());
        }
    }

    @Override
    public void drawUpdateIcon(Minecraft minecraft, Update update, int x, int y) {
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        int vOffset = update.animated() && (System.currentTimeMillis() / 800 & 1) == 1 ? 8 : 0;
        minecraft.getTextureManager().bindTexture(update.textures());
        ClientHelper.drawModalRectWithCustomSizedTexture(x, y, update.texOffset() * 8, vOffset, 8, 8, 64, 16);
    }

    @Nullable
    @Override
    public String getUpdateText(Update update) {
        if (update != null && update.homepage() != null && !update.homepage().trim().isEmpty()) {
            return I18n.format("catalogue.gui.update_available_no_version", update.homepage());
        }
        return null;
    }

    @Nullable
    @Override
    public IResourcePack getResourcePack() {
        return FMLClientHandler.instance().getResourcePackFor(this.getModId());
    }

    @Nullable
    private ModMetadata getMetadata() {
        ModMetadata metadata = this.info.getMetadata();
        return metadata != null && !metadata.autogenerated ? metadata : null;
    }

    private Type analyzeType(@Nonnull ModContainer info) {
        if (LIB_MODS.contains(info.getModId())) {
            return Type.LIBRARY;
        } else {
            return Type.DEFAULT;
        }
    }

    private Set<String> analyzeDependencies(@Nonnull ModContainer source) {
        List<? extends ArtifactVersion> versions = source.getDependencies();
        return versions.stream()
            .map(ArtifactVersion::getLabel)
            .filter(modid -> !IGNORED_DEPENDENCIES.contains(modid))
            .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }
}
