package net.zhaiji.grindstoneplus;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(GrindstonePlus.MOD_ID)
public class GrindstonePlus {
    public static final String MOD_ID = "grindstone_plus";

    public GrindstonePlus(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, GrindstonePlusConfig.SPEC);
        modEventBus.addListener(GrindstonePlusConfig::handlerModConfigEvent);
    }
}
