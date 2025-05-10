package net.zhaiji.grindstoneplus.compat;

import net.neoforged.fml.ModList;

public class TaxFreeLevelsCompat {
    public static final String MOD_ID = "taxfreelevels";

    public static boolean isLoad(){
        return ModList.get().isLoaded(TaxFreeLevelsCompat.MOD_ID);
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
            cost += TaxFreeLevelsCompat.getXpNeededForNextLevel(i);
        }
        return cost;
    }

    public static int computeCost(int form, int to) {
        int cost = 0;
        for (int i = form; i < to; i++) {
            cost += TaxFreeLevelsCompat.getXpNeededForNextLevel(i);
        }
        return cost;
    }
}
