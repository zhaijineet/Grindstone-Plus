package net.zhaiji.grindstoneplus.compat;

public class TaxFreeLevelsCompat {
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
