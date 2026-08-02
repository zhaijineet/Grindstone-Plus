package net.zhaiji.grindstoneplus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.zhaiji.grindstoneplus.datagen.DataGenHandler;

@Mod(GrindstonePlus.MOD_ID)
public class GrindstonePlus {
    public static final String MOD_ID = "grindstone_plus";

    public GrindstonePlus(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.SERVER, GrindstonePlusConfig.SPEC);
        modEventBus.addListener(DataGenHandler::handlerGatherDataEvent);
    }
}
