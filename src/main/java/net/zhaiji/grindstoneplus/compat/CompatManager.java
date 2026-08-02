package net.zhaiji.grindstoneplus.compat;

import net.minecraftforge.fml.loading.LoadingModList;

public class CompatManager {
    public static final boolean CLOTH_CONFIG_LOADED = LoadingModList.get().getModFileById("cloth_config") != null;

    public static final boolean TAX_FREE_LEVELS_LOADED = LoadingModList.get().getModFileById("taxfreelevels") != null;
}
