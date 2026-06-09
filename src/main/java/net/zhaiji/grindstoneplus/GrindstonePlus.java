package net.zhaiji.grindstoneplus;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GrindstonePlus.MOD_ID)
public class GrindstonePlus {
    public static final String MOD_ID = "grindstone_plus";

    public GrindstonePlus() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GrindstonePlusConfig.SPEC);
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        modBus.addListener(GrindstonePlusConfig::handlerModConfigEvent);
    }
}
