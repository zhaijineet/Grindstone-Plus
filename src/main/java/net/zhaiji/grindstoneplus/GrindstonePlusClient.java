package net.zhaiji.grindstoneplus;

import net.minecraftforge.eventbus.api.IEventBus;
import net.zhaiji.grindstoneplus.compat.ClothConfigCompat;
import net.zhaiji.grindstoneplus.compat.CompatManager;

public class GrindstonePlusClient {
    public static void init(IEventBus modEventBus, IEventBus gameEventBus) {
        if (CompatManager.CLOTH_CONFIG_LOADED) {
            ClothConfigCompat.registerConfigScreen();
        }
    }
}
