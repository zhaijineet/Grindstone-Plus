package net.zhaiji.grindstoneplus.compat;

import net.minecraftforge.fml.ModList;

public class TaxFreeLevelsCompat {
    public static final String MOD_ID = "taxfreelevels";

    private static boolean TFLLoad = ModList.get().isLoaded(MOD_ID);

    public static boolean isLoad() {
        return TFLLoad;
    }

    private static int getXpNeededForNextLevel(int level) {
        if (level >= 30) {
            return 112 + (level - 30) * 9;
        } else {
            return level >= 15 ? 37 + (level - 15) * 5 : 7 + level * 2;
        }
    }

    public static int computeCost(int level) {
        int cost = 0;
        for (int i = 0; i < level; i++) {
            cost += getXpNeededForNextLevel(i);
        }
        return cost;
    }
}
