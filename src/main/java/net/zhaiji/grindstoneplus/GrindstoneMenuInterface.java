package net.zhaiji.grindstoneplus;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;

public interface GrindstoneMenuInterface {
    boolean canCreateResult();

    Map<Enchantment, Integer> getEnchantments(ItemStack stack);

    ItemStack createResultItem(ItemStack stack);

    boolean needCost();

    int getCost();

    boolean canTransferCurses();
}
