package com.cleanroommc.catalogue.platform;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.client.Branding;
import com.cleanroommc.catalogue.client.ForgeModData;
import com.cleanroommc.catalogue.client.IModData;
import com.cleanroommc.catalogue.platform.services.IPlatformHelper;
import com.google.common.base.Strings;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiVideoSettings;

import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public List<IModData> getAllModData() {
        List<IModData> dataList = new ArrayList<>();
        // Handle special containers
        ArrayList<ModContainer> specialContainerList = new ArrayList<>();
        FMLClientHandler.instance().addSpecialModEntries(specialContainerList);
        specialContainerList.removeIf(modContainer -> {
            if (modContainer.getModId().equals("optifine")) {
                dataList.add(new OFData(modContainer));
                return true;
            }
            return false;
        });

        // Handle normal containers
        List<ModContainer> containerList = new ArrayList<>();
        for (ModContainer mod : Loader.instance().getActiveModList()) {
            if (mod.getMetadata() != null && mod.getMetadata().parentMod == null && !Strings.isNullOrEmpty(mod.getMetadata().parent)) {
                String parentMod = mod.getMetadata().parent;
                ModContainer parentContainer = Loader.instance().getIndexedModList().get(parentMod);
                if (parentContainer != null) {
                    mod.getMetadata().parentMod = parentContainer;
                    parentContainer.getMetadata().childMods.add(mod);
                    continue;
                }
            } else if (mod.getMetadata() != null && mod.getMetadata().parentMod != null) {
                continue;
            }
            containerList.add(mod);
        }

        containerList.addAll(specialContainerList);
        dataList.addAll(containerList.stream().map(ForgeModData::new).collect(Collectors.toList()));
        return dataList;
    }

    @Override
    public File getModDirectory() {
        return Loader.instance().getConfigDir().getParentFile().toPath().resolve("mods").toFile();
    }

    @Override
    public Path getConfigDirectory() {
        return Loader.instance().getConfigDir().toPath();
    }

    @Override
    public BufferedImage loadImageFromModResource(String modid, String resource) throws IOException {
        InputStream is = getClass().getResourceAsStream(resource);
        return is != null ? ImageIO.read(is) : null;
    }

    @Override
    public boolean isModLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }

    @Override
    public boolean getEnableBannerLimit() {
        return CatalogueConfig.enableBannerLimit;
    }

    @Override
    public Branding.BannerLimit getBannerLimit() {
        return new Branding.BannerLimit(CatalogueConfig.bannerMaxWidth, CatalogueConfig.bannerMaxHeight);
    }

    @Override
    public boolean getEnableIconLimit() {
        return CatalogueConfig.enableIconLimit;
    }

    @Override
    public int getIconLimit() {
        return CatalogueConfig.iconMaxWidthHeight;
    }

    private static class OFData extends ForgeModData {
        private OFData(ModContainer info) {
            super(info);
        }

        @Override
        public String getVersion() {
            String version = this.getInnerVersion();
            return version.substring(version.indexOf("OptiFine_1.12.2_") + 16);
        }

        @Nullable
        @Override
        public String getDescription() {
            // Copied from https://www.optifine.net/home
            return """
                OptiFine is a Minecraft optimization mod.
                It allows Minecraft to run faster and look better with full support for HD textures and many configuration options.
                """;
        }

        @Nullable
        @Override
        public String getLicense() {
            return "All Rights Reserved";
        }

        @Nullable
        @Override
        public String getAuthors() {
            return "sp614x";
        }

        @Nullable
        @Override
        public String getHomepage() {
            return "https://www.optifine.net/home";
        }

        @Nullable
        @Override
        public String getIssueTracker() {
            return "https://github.com/sp614x/optifine/issues";
        }

        @Override
        public boolean hasConfig() {
            return true;
        }

        @Override
        public void openConfigScreen(Minecraft minecraft, GuiScreen parent) {
            minecraft.displayGuiScreen(new GuiVideoSettings(parent, minecraft.gameSettings));
        }
    }
}
