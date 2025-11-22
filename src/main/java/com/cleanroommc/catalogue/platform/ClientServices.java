package com.cleanroommc.catalogue.platform;

import com.cleanroommc.catalogue.CatalogueConstants;
import com.cleanroommc.catalogue.platform.services.IPlatformHelper;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ClientServices {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static <T> T load(Class<T> clazz) {
        Iterator<T> iterator = ServiceLoader.load(clazz).iterator();
        if (iterator.hasNext()) {
            final T loadedService = iterator.next();
            CatalogueConstants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
            return loadedService;
        } else {
            throw new NullPointerException("Failed to load service for " + clazz.getName());
        }
    }
}
