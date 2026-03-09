package com.cleanroommc.catalogue;

import com.cleanroommc.catalogue.config.util.ConfigBuilder;
import net.minecraftforge.common.config.Configuration;

public class CatalogueConfig {
    public static boolean enable = true;
    public static String[] libraryList = new String[]{
            "Forge",
            "FML",
            "mcp",
            "gtnhlib",
            "hodgepodge",
            "unimixins",
            "lwjgl3ify"
    };
    public static String[] ignoredDependenciesList = new String[]{
            "minecraft",
            "Forge",
            "FML",
            "mcp"
    };
    public static boolean enableBannerLimit = false;
    public static int bannerMaxWidth = 1280;
    public static int bannerMaxHeight = 256;
    public static boolean enableIconLimit = false;
    public static int iconMaxWidthHeight = 256;
    public static boolean guiConfigsOnly = false;
    public static boolean guiFavouritesOnly = false;
    public static int guiSortMode = 0;
    public static boolean guiHideLibraries = true;
    public static boolean guiHideChildMods = true;

    /// Inner method, do not call
    public static void build(ConfigBuilder builder) {
        builder.pushCategory(Configuration.CATEGORY_GENERAL, null, null);

        enable = builder.get(
                "enable",
                enable,
                "Whether enable Catalogue. \nSetting it false will stop Catalogue redirecting Forge's mod list calls."
        );

        libraryList = builder.getProp(
                "libraryList",
                libraryList,
                "The list of library mods' mod ids. \nThey will have grey names in the mod list."
        ).setRequiresMcRestart(true).getStringList();

        ignoredDependenciesList = builder.getProp(
                "ignoredDependenciesList",
                ignoredDependenciesList,
                "The list of ignored dependencies' mod ids. \nThey will not be displayed when searching for dependencies/dependants."
        ).setRequiresMcRestart(true).getStringList();

        enableBannerLimit = builder.getProp(
                "enableBannerLimit",
                enableBannerLimit,
                "Whether limit the size of mods' banners."
        ).setRequiresMcRestart(true).getBoolean();

        bannerMaxWidth = builder.getProp(
                "bannerMaxWidth",
                bannerMaxWidth,
                "The maximum of banner's width. Will not work if Enable Banner Limit is set false."
        ).setMinValue(0).setRequiresMcRestart(true).getInt();

        bannerMaxHeight = builder.getProp(
                "bannerMaxHeight",
                bannerMaxHeight,
                "The maximum of banner's height. Will not work if Enable Banner Limit is set false."
        ).setMinValue(0).setRequiresMcRestart(true).getInt();

        enableIconLimit = builder.getProp(
                "enableIconLimit",
                enableIconLimit,
                "Whether limit the size of mods' icons."
        ).setRequiresMcRestart(true).getBoolean();

        iconMaxWidthHeight = builder.getProp(
                "iconMaxWidthHeight",
                iconMaxWidthHeight,
                "The maximum of icon's width and height. Will not work if Enable Icon Limit is set false."
        ).setMinValue(0).setRequiresMcRestart(true).getInt();

        guiConfigsOnly = builder.getProp(
                "guiConfigsOnly",
                guiConfigsOnly,
                "Whether Catalogue starts with the 'mods with configs only' filter enabled."
        ).getBoolean();

        guiFavouritesOnly = builder.getProp(
                "guiFavouritesOnly",
                guiFavouritesOnly,
                "Whether Catalogue starts with the 'favourites only' filter enabled."
        ).getBoolean();

        guiSortMode = builder.getProp(
                "guiSortMode",
                guiSortMode,
                "Catalogue mod list sort mode. 0 = A to Z, 1 = Z to A, 2 = favourites first."
        ).setMinValue(0).setMaxValue(2).getInt();

        guiHideLibraries = builder.getProp(
                "guiHideLibraries",
                guiHideLibraries,
                "Whether Catalogue hides library mods by default."
        ).getBoolean();

        guiHideChildMods = builder.getProp(
                "guiHideChildMods",
                guiHideChildMods,
                "Whether Catalogue hides child mods by default."
        ).getBoolean();

        builder.popCategoryWithoutLangKey();
    }
}
