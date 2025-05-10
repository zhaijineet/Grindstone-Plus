package net.zhaiji.grindstoneplus;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public interface GrindstoneMenuInterface {
    boolean canCreateResult();

    ItemEnchantments getEnchantments(ItemStack stack);

    ItemStack createResultItem(ItemStack stack);

    boolean needCost();

    int getCost();

    boolean canTransferCurses();
}
