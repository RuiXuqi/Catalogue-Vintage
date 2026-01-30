package com.cleanroommc.catalogue;

import net.minecraftforge.common.config.Configuration;

import java.io.File;

public class CatalogueConfig {
    private static Configuration config;

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

    public static void init(File configFile) {
        if (config == null) {
            config = new Configuration(configFile);
            config.load();
        }
        syncConfig();
    }

    public static void syncConfig() {
        enable = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "enable",
                        enable,
                        "Whether enable Catalogue. \nSetting it false will stop Catalogue redirecting Forge's mod list calls."
                ).setLanguageKey("catalogue.config.enable")
                .getBoolean();

        libraryList = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "libraryList",
                        libraryList,
                        "The list of library mods' mod ids. \nThey will have grey names in the mod list."
                ).setLanguageKey("catalogue.config.library_list")
                .setRequiresMcRestart(true)
                .getStringList();

        ignoredDependenciesList = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "ignoredDependenciesList",
                        ignoredDependenciesList,
                        "The list of ignored dependencies' mod ids. \nThey will not be displayed when searching for dependencies/dependants."
                ).setLanguageKey("catalogue.config.ignored_dependencies_list")
                .setRequiresMcRestart(true)
                .getStringList();

        enableBannerLimit = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "enableBannerLimit",
                        enableBannerLimit,
                        "Whether limit the size of mods' banners."
                ).setLanguageKey("catalogue.config.enable_banner_limit")
                .setRequiresMcRestart(true)
                .getBoolean();

        bannerMaxWidth = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "bannerMaxWidth",
                        bannerMaxWidth,
                        "The maximum of banner's width. Will not work if Enable Banner Limit is set false."
                ).setLanguageKey("catalogue.config.banner_max_width")
                .setMinValue(0)
                .setRequiresMcRestart(true)
                .getInt();

        bannerMaxHeight = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "bannerMaxHeight",
                        bannerMaxHeight,
                        "The maximum of banner's height. Will not work if Enable Banner Limit is set false."
                ).setLanguageKey("catalogue.config.banner_max_height")
                .setMinValue(0)
                .setRequiresMcRestart(true)
                .getInt();

        enableIconLimit = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "enableIconLimit",
                        enableIconLimit,
                        "Whether limit the size of mods' icons."
                ).setLanguageKey("catalogue.config.enable_icon_limit")
                .setRequiresMcRestart(true)
                .getBoolean();

        iconMaxWidthHeight = config.get(
                        Configuration.CATEGORY_GENERAL,
                        "iconMaxWidthHeight",
                        iconMaxWidthHeight,
                        "The maximum of icon's width and height. Will not work if Enable Icon Limit is set false."
                ).setLanguageKey("catalogue.config.icon_max_width_height")
                .setMinValue(0)
                .setRequiresMcRestart(true)
                .getInt();

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static Configuration getConfig() {
        return config;
    }
}
