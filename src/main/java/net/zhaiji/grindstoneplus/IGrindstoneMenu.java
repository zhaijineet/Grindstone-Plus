package net.zhaiji.grindstoneplus;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public interface IGrindstoneMenu {
    boolean canCreateResult();

    Map<Enchantment, Integer> getEnchantments(ItemStack stack);

    ItemStack createResultItem(ItemStack stack);

    boolean needCost();

    int getCost();

    boolean canTransferCurses();

    boolean isSplitLevelMode();

    boolean isModResult();

    static int calculateDecreasedRepairCost(int currentCost) {
        return Math.max(0, (currentCost - 1) / 2);
    }
}
