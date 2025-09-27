package com.cleanroommc.catalogue.platform;

import com.cleanroommc.catalogue.client.ForgeModData;
import com.cleanroommc.catalogue.client.IModData;
import com.cleanroommc.catalogue.platform.services.IPlatformHelper;
import net.minecraftforge.fml.common.Loader;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Author: MrCrayfish
 */
public class ForgePlatformHelper implements IPlatformHelper {
    @Override
    public boolean isForge() {
        return true;
    }

    @Override
    public List<IModData> getAllModData() {
        return Loader.instance().getActiveModList().stream().map(ForgeModData::new).collect(Collectors.toList());
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
    public boolean isModLoaded(String modId) {
        return Loader.isModLoaded(modId);
    }
}
