package net.zhaiji.grindstoneplus.compat;

import net.neoforged.fml.loading.FMLLoader;

public class CompatManager {
    public static final boolean TAX_FREE_LEVELS_LOADED = FMLLoader.getCurrent().getLoadingModList().getModFileById("taxfreelevels") != null;
}
