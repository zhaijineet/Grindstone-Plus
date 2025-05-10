package net.zhaiji.grindstoneplus;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(GrindstonePlus.MOD_ID)
public class GrindstonePlus {
    public static final String MOD_ID = "grindstone_plus";

    public GrindstonePlus() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, GrindstonePlusConfig.SPEC);
    }
}
