package com.cleanroommc.catalogue.config;

import com.cleanroommc.catalogue.CatalogueConfig;
import com.cleanroommc.catalogue.config.util.ConfigBuilder;
import com.cleanroommc.catalogue.config.util.IFormatter;
import cpw.mods.fml.client.config.IConfigElement;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.List;

public final class Config {
    private static Configuration config;

    public static void init(File configFile) {
        if (config != null) throw new IllegalStateException("Init have been performed!");
        config = new Configuration(configFile);
        readFromFile();
    }

    public static void readFromFile() {
        build(ConfigBuilder.startReadingFromFile(config));
    }

    public static void readFromProp() {
        build(ConfigBuilder.startReadingFromProp(config));
    }

    public static void saveToFile() {
        build(ConfigBuilder.startSaving(config));
    }

    private static void build(@Nonnull ConfigBuilder builder) {
        builder.setLangKeyPrefix("catalogue.config");
        builder.setLangKeyFormatter(IFormatter.CAMEL_TO_SNAKE);

        CatalogueConfig.build(builder);

        builder.finishBuilding();
    }

    public static Configuration getConfig() {
        return config;
    }

    @SuppressWarnings("rawtypes")
    @Nonnull
    public static List<IConfigElement> getRootConfigElements() {
        return new ConfigElement<>(config.getCategory(Configuration.CATEGORY_GENERAL)).getChildElements();
    }
}
