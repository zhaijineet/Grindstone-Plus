package net.zhaiji.grindstoneplus;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.zhaiji.grindstoneplus.datagen.DataGenHandler;

@Mod(GrindstonePlus.MOD_ID)
public class GrindstonePlus {
    public static final String MOD_ID = "grindstone_plus";

    public GrindstonePlus() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        IEventBus gameEventBus = MinecraftForge.EVENT_BUS;

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, GrindstonePlusConfig.SPEC);
        modEventBus.addListener(DataGenHandler::handlerGatherDataEvent);

        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> GrindstonePlusClient.init(modEventBus, gameEventBus));
    }
}
