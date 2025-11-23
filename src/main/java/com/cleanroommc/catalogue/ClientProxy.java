package com.cleanroommc.catalogue;

import com.cleanroommc.catalogue.client.ClientHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nonnull;

@SuppressWarnings("unused")
public class ClientProxy extends CommonProxy {
    @Override
    public void init(@Nonnull FMLInitializationEvent event) {
        super.init(event);
        MinecraftForge.EVENT_BUS.register(new ClientHandler());
    }
}
