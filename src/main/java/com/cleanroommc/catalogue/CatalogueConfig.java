package com.cleanroommc.catalogue;

import com.cleanroommc.catalogue.config.util.ConfigBuilder;
import net.minecraftforge.common.config.Configuration;

import java.util.regex.Pattern;

public class CatalogueConfig {
    private static final String[] CUSTOM_MOD_INFO_FIELDS = new String[]{
            "name", "description", "url", "issueTrackerUrl",
            "logoFile", "iconFile", "iconItem", "backgroundFile", "license", "credits"
    };
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
    public static String[] customModInfo = new String[]{};
    public static boolean enableBannerLimit = false;
    public static int bannerMaxWidth = 1280;
    public static int bannerMaxHeight = 256;
    public static boolean enableIconLimit = false;
    public static int iconMaxWidthHeight = 256;

    /// Internal method, do not call
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

        customModInfo = builder.getProp(
                "customModInfo",
                customModInfo,
                "Custom mod info entries. \nFormat: modid:field1=value,field2=value2 \nAvailable fields: name, description, url, issueTrackerUrl, logoFile, iconFile, iconItem, backgroundFile, license, credits"
        ).setRequiresMcRestart(true).setValidationPattern(Pattern.compile(
                "^[^:]+:(" + String.join("|", CUSTOM_MOD_INFO_FIELDS) + ")=[^,]*(,(" + String.join("|", CUSTOM_MOD_INFO_FIELDS) + ")=[^,]*)*$"
        )).getStringList();

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

        builder.popCategoryWithoutLangKey();
    }
}
