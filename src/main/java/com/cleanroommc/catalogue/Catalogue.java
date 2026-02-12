package com.cleanroommc.catalogue;

import net.minecraftforge.fml.common.Mod;

/**
 * Author: MrCrayfish
 */
@Mod(
        modid = Reference.MOD_ID,
        name = Reference.MOD_NAME,
        version = Reference.VERSION,
        clientSideOnly = true,
        acceptableRemoteVersions = "*",
        customProperties = {
                @Mod.CustomProperty(k = "license", v = "MIT"),
                @Mod.CustomProperty(k = "issueTrackerUrl", v = "https://github.com/RuiXuqi/Catalogue-Vintage/issues"),
                @Mod.CustomProperty(k = "iconFile", v = "assets/catalogue/icon.png"),
                @Mod.CustomProperty(k = "backgroundFile", v = "assets/catalogue/background.png")
        }
)
public class Catalogue {
}
