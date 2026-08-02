package net.zhaiji.grindstoneplus.compat;

import net.neoforged.fml.loading.LoadingModList;

public class CompatManager {
    public static final boolean TAX_FREE_LEVELS_LOADED = LoadingModList.get().getModFileById("taxfreelevels") != null;
}
