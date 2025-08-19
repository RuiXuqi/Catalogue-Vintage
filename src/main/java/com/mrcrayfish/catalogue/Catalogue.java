package com.mrcrayfish.catalogue;

import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Author: MrCrayfish
 */
@Mod(modid = Reference.MOD_ID, name = Reference.MOD_NAME, version = Reference.VERSION, dependencies = "required-after:cleanroom")
public class Catalogue {

    public static final Logger LOGGER = LogManager.getLogger("catalogue");

    public Catalogue() {
    }

}
