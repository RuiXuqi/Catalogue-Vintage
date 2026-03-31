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
import java.util.*;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ForgeModData implements IModData {
    public static final List<String> LIB_MODS = Arrays.asList(CatalogueConfig.libraryList);
    public static final List<String> IGNORED_DEPENDENCIES = Arrays.asList(CatalogueConfig.ignoredDependenciesList);

    private final @Nonnull ModContainer info;
    private final @Nullable ModMetadata metadata;
    private final Type type;
    private final Set<String> dependencies;
    private final Set<String> childMods;
    private final String modId;

    public ForgeModData(@Nonnull ModContainer info) {
        this.info = info;
        this.metadata = info.getMetadata();
        this.type = this.analyzeType(info);
        this.dependencies = analyzeDependencies(info);
        this.childMods = analyzeChildMods(info);
        this.modId = this.info.getModId();
    }

    @Override
    public Type getType() {
        return this.type;
    }

    @Override
    public String getModId() {
        return this.modId;
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
        return this.metadata != null ? this.metadata.description : null;
    }

    @Nullable
    @Override
    public String getItemIcon() {
        return this.info.getCustomModProperties().get("iconItem");
    }

    @Nullable
    @Override
    public String getImageIcon() {
        return this.info.getCustomModProperties().get("iconFile");
    }

    @Nullable
    @Override
    public String getLicense() {
        return this.info.getCustomModProperties().get("license");
    }

    @Nullable
    @Override
    public String getCredits() {
        return this.metadata != null ? this.metadata.credits : null;
    }

    @Nullable
    @Override
    public String getAuthors() {
        return this.metadata != null ? this.metadata.getAuthorList() : null;
    }

    @Nullable
    @Override
    public String getHomepage() {
        return this.metadata != null ? this.metadata.url : null;
    }

    @Nullable
    @Override
    public String getIssueTracker() {
        return this.info.getCustomModProperties().get("issueTrackerUrl");
    }

    @Nullable
    @Override
    public String getBanner() {
        return this.metadata != null ? this.metadata.logoFile : null;
    }

    @Nullable
    @Override
    public String getBackground() {
        return this.info.getCustomModProperties().get("backgroundFile");
    }

    @Nullable
    @Override
    public String getChildModNames() {
        return this.metadata != null ? this.metadata.getChildModList() : null;
    }

    @Nullable
    @Override
    public String getParentModName() {
        return this.metadata != null && this.metadata.parentMod != null ? this.metadata.parentMod.getName() : null;
    }

    @Nullable
    @Override
    public Update getUpdate() {
        return null;
    }

    @Nonnull
    @Override
    public Set<String> getDependencies() {
        return this.dependencies;
    }

    @Nonnull
    @Override
    public Set<String> getChildMods() {
        return this.childMods;
    }

    @Override
    public boolean hasConfig() {
        ensureCarbonConfigsRegistered();
        IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
        if (guiFactory != null && guiFactory.mainConfigGuiClass() != null) {
            return true;
        }
        return this.hasGtnhLibConfig();
    }

    private boolean hasGtnhLibConfig() {
        try {
            Class<?> managerClass = Class.forName("com.gtnewhorizon.gtnhlib.config.ConfigurationManager");
            return Boolean.TRUE.equals(
                    managerClass.getMethod("isModRegistered", String.class).invoke(null, this.getModId()));
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    @Override
    public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
        try {
            IModGuiFactory guiFactory = FMLClientHandler.instance().getGuiFactoryFor(this.info);
            if (guiFactory != null && guiFactory.mainConfigGuiClass() != null) {
                GuiScreen newScreen;
                try {
                    newScreen = guiFactory.mainConfigGuiClass().getConstructor(GuiScreen.class).newInstance(parent);
                } catch (NoSuchMethodException e) {
                    newScreen = (GuiScreen) guiFactory.getClass()
                            .getMethod("createConfigGui", GuiScreen.class).invoke(guiFactory, parent);
                }
                minecraft.displayGuiScreen(newScreen);
                return;
            }
        } catch (Exception e) {
            CatalogueConstants.LOG.error("There was a critical issue trying to build the config GUI for {}", this.getModId());
        }
        GuiScreen gtnhLibScreen = this.createGtnhLibConfigScreen(parent);
        if (gtnhLibScreen != null) {
            minecraft.displayGuiScreen(gtnhLibScreen);
            //return;
        }
    }

    @Nullable
    private GuiScreen createGtnhLibConfigScreen(GuiScreen parent) {
        if (!this.hasGtnhLibConfig()) {
            return null;
        }

        try {
            Class<?> guiClass = Class.forName("com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig");
            return (GuiScreen) guiClass
                    .getConstructor(GuiScreen.class, String.class, String.class)
                    .newInstance(parent, this.getModId(), this.getDisplayName());
        } catch (ReflectiveOperationException | LinkageError e) {
            CatalogueConstants.LOG.error("Failed to create GTNHLib config GUI for {}", this.getModId(), e);
            return null;
        }
    }

    private static boolean carbonConfigRegistrationAttempted;

    private static void ensureCarbonConfigsRegistered() {
        if (carbonConfigRegistrationAttempted) return;
        carbonConfigRegistrationAttempted = true;
        try {
            Class<?> cls = Class.forName("carbonconfiglib.impl.internal.EventHandler");
            java.lang.reflect.Method m = cls.getDeclaredMethod("registerConfigs");
            m.setAccessible(true);
            m.invoke(cls.getField("INSTANCE").get(null));
        } catch (ReflectiveOperationException | LinkageError ignored) {
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

    private Type analyzeType(@Nonnull ModContainer info) {
        if (this.metadata != null && this.metadata.parentMod != null) {
            return Type.CHILD;
        } else if (LIB_MODS.contains(info.getModId())) {
            return Type.LIBRARY;
        } else {
            return Type.DEFAULT;
        }
    }

    private static @Nonnull Set<String> analyzeDependencies(@Nonnull ModContainer source) {
        List<? extends ArtifactVersion> versions = source.getDependencies();
        return versions.stream()
                .map(ArtifactVersion::getLabel)
                .filter(modid -> !IGNORED_DEPENDENCIES.contains(modid))
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }

    private static @Nonnull Set<String> analyzeChildMods(@Nonnull ModContainer source) {
        ModMetadata metadata = source.getMetadata();
        if (metadata == null) return Collections.emptySet();
        return metadata.childMods.stream()
                .filter(Objects::nonNull)
                .map(ModContainer::getModId)
                .collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    }
}
