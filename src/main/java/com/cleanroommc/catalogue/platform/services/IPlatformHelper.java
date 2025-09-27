package com.cleanroommc.catalogue.platform.services;

import com.cleanroommc.catalogue.client.IModData;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public interface IPlatformHelper {
    List<IModData> getAllModData();

    File getModDirectory();

    Path getConfigDirectory();

    boolean isModLoaded(String modId);

    default boolean isForge() {
        return false;
    }
}