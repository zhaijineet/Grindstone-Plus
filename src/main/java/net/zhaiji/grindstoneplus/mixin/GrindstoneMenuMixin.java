package net.zhaiji.grindstoneplus.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.zhaiji.grindstoneplus.GrindstonePlusConfig;
import net.zhaiji.grindstoneplus.IGrindstoneMenu;
import net.zhaiji.grindstoneplus.compat.CompatManager;
import net.zhaiji.grindstoneplus.compat.TaxFreeLevelsCompat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin extends AbstractContainerMenu implements IGrindstoneMenu {
    @Shadow
    public Container repairSlots;
    @Unique
    private boolean isModResult;
    @Unique
    private boolean wasExtractMode;
    @Unique
    private ItemStack lastInputItem;
    @Unique
    private int cost;
    @Final
    @Shadow
    private Container resultSlots;
    @Shadow
    private int xp;

    public GrindstoneMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(
        method = "createResult",
        at = @At("HEAD"),
        cancellable = true
    )
    public void grindstonePlus$createResult(CallbackInfo ci) {
        cost = 0;
        isModResult = false;
        xp = -1;
        if (canCreateResult()) {
            ItemStack itemStack1 = repairSlots.getItem(0);
            if (itemStack1 != lastInputItem) {
                wasExtractMode = false;
                lastInputItem = itemStack1;
            }
            ItemStack itemStack2 = repairSlots.getItem(1);
            GrindstoneEvent.OnPlaceItem event = new GrindstoneEvent.OnPlaceItem(itemStack1, itemStack2, xp);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                xp = -1;
                resultSlots.setItem(0, ItemStack.EMPTY);
            } else if (event.getOutput().isEmpty()) {
                isModResult = true;
                xp = Integer.MIN_VALUE;
                resultSlots.setItem(0, createResultItem(itemStack1));
            } else {
                xp = event.getXp();
                resultSlots.setItem(0, event.getOutput());
            }
            broadcastChanges();
            if (!isSplitLevelMode()) {
                wasExtractMode = true;
            }
            ci.cancel();
        } else {
            wasExtractMode = false;
        }
    }

    @Override
    public boolean canCreateResult() {
        ItemStack itemStack1 = repairSlots.getItem(0);
        ItemStack itemStack2 = repairSlots.getItem(1);
        if (!itemStack2.is(Items.BOOK)) return false;

        ItemEnchantments enchantments = getEnchantments(itemStack1);
        if (enchantments.isEmpty()) return false;

        boolean hasExtractable = canTransferCurses();
        if (!hasExtractable) {
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                if (!entry.getKey().is(EnchantmentTags.CURSE)) {
                    hasExtractable = true;
                    break;
                }
            }
        }
        if (!hasExtractable) return false;

        boolean isEnchantedBook = itemStack1.is(Items.ENCHANTED_BOOK);
        boolean canExtract = enchantments.size() > 1 || !isEnchantedBook;
        boolean canSplitLevel = isEnchantedBook && enchantments.size() == 1 && enchantments.entrySet().iterator().next().getIntValue() > 1;

        return canExtract || canSplitLevel;
    }

    @Override
    public ItemEnchantments getEnchantments(ItemStack stack) {
        DataComponentType<ItemEnchantments> datacomponenttype = EnchantmentHelper.getComponentType(stack);
        ItemEnchantments itemenchantments = stack.get(datacomponenttype);
        return Objects.requireNonNullElse(itemenchantments, ItemEnchantments.EMPTY);
    }

    @Override
    public ItemStack createResultItem(ItemStack stack) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        boolean isEnchantedBook = stack.is(Items.ENCHANTED_BOOK);

        if (isSplitLevelMode()) {
            Object2IntMap.Entry<Holder<Enchantment>> entry =
                getEnchantments(stack).entrySet().iterator().next();
            EnchantmentHelper.updateEnchantments(enchantedBook, mutable ->
                mutable.set(entry.getKey(), entry.getIntValue() - 1)
            );
        } else {
            EnchantmentHelper.updateEnchantments(enchantedBook, mutable -> {
                for (Object2IntMap.Entry<Holder<Enchantment>> entry : getEnchantments(stack).entrySet()) {
                    if (!canTransferCurses() && entry.getKey().is(EnchantmentTags.CURSE)) {
                        continue;
                    }
                    mutable.set(entry.getKey(), entry.getIntValue());
                    if (isEnchantedBook) {
                        break;
                    }
                }
            });
        }

        int inputRepairCost = stack.getOrDefault(DataComponents.REPAIR_COST, 0);
        enchantedBook.set(DataComponents.REPAIR_COST, IGrindstoneMenu.calculateDecreasedRepairCost(inputRepairCost));

        // 计算经验消耗（基于输入物品的附魔总数）
        int enchantCount = getEnchantments(stack).size();
        GrindstonePlusConfig.CostType costType = GrindstonePlusConfig.COST_TYPE_ENUM_VALUE.get();
        if (costType == GrindstonePlusConfig.CostType.COUNT_COST) {
            cost = enchantCount;
        } else if (costType == GrindstonePlusConfig.CostType.FIXED_COST) {
            cost = GrindstonePlusConfig.FIXED_COST_VALUE.get();
        } else if (costType == GrindstonePlusConfig.CostType.ANVIL_COST) {
            for (int index = 0; index < enchantCount; index++) {
                cost = AnvilMenu.calculateIncreasedRepairCost(cost);
            }
            if (cost > 40) {
                cost = 39;
            }
        }

        return enchantedBook;
    }

    @Override
    public boolean needCost() {
        return GrindstonePlusConfig.COST_TYPE_ENUM_VALUE.get() != GrindstonePlusConfig.CostType.NO_COST;
    }

    @Override
    public int getCost() {
        return cost;
    }

    @Override
    public boolean canTransferCurses() {
        return GrindstonePlusConfig.TRANSFER_CURSES.get();
    }

    @Override
    public boolean isSplitLevelMode() {
        ItemStack itemStack1 = repairSlots.getItem(0);
        if (!itemStack1.is(Items.ENCHANTED_BOOK)) return false;
        ItemEnchantments enchantments = getEnchantments(itemStack1);
        if (enchantments.size() != 1) return false;
        return enchantments.entrySet().iterator().next().getIntValue() > 1;
    }

    @Override
    public boolean isModResult() {
        return isModResult;
    }

    @Inject(
        method = "quickMoveStack",
        at = @At("HEAD"),
        cancellable = true
    )
    public void grindstonePlus$quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        if (index == 2 && isModResult()) {
            if (isSplitLevelMode() && wasExtractMode) {
                wasExtractMode = false;
                cir.setReturnValue(ItemStack.EMPTY);
                cir.cancel();
                return;
            }
            if (!player.hasInfiniteMaterials() && needCost()) {
                if (player.experienceLevel < getCost()) {
                    cir.setReturnValue(ItemStack.EMPTY);
                    cir.cancel();
                }
            }
        }
    }

    @Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$3")
    public abstract static class GrindstoneMenu$3Mixin extends Slot {
        public GrindstoneMenu$3Mixin(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Inject(
            method = "mayPlace",
            at = @At("HEAD"),
            cancellable = true
        )
        public void grindstonePlus$mayPlace(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(true);
        }
    }

    @Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$4")
    public abstract static class GrindstoneMenu$4Mixin extends Slot {
        public GrindstoneMenu$4Mixin(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPickup(Player player) {
            IGrindstoneMenu menuInterface = (IGrindstoneMenu) player.containerMenu;
            if (player.hasInfiniteMaterials() || !menuInterface.needCost()) return true;
            return player.experienceLevel >= menuInterface.getCost();
        }

        @Inject(
            method = "onTake",
            at = @At("HEAD"),
            cancellable = true
        )
        public void grindstonePlus$onTake(Player player, ItemStack stack, CallbackInfo ci) {
            GrindstoneMenu menu = (GrindstoneMenu) player.containerMenu;
            IGrindstoneMenu menuInterface = (IGrindstoneMenu) player.containerMenu;
            if (menuInterface.canCreateResult()) {
                ItemStack itemStack1 = menu.repairSlots.getItem(0);
                ItemStack itemStack2 = menu.repairSlots.getItem(1);
                if (!NeoForge.EVENT_BUS.post(new GrindstoneEvent.OnTakeItem(menu.access, player, itemStack1, itemStack2, 0)).isCanceled()) {
                    menu.access.execute((level, pos) -> {
                        level.levelEvent(1042, pos, 0);
                    });
                    if (menuInterface.isModResult()) {
                        if (!player.hasInfiniteMaterials() && menuInterface.needCost()) {
                            if (CompatManager.TAX_FREE_LEVELS_LOADED) {
                                player.giveExperiencePoints(-TaxFreeLevelsCompat.computeCost(menuInterface.getCost()));
                            } else {
                                player.giveExperienceLevels(-menuInterface.getCost());
                            }
                        }
                        repairTransition(itemStack1, menuInterface);
                    }
                    itemStack2.shrink(1);
                    menu.repairSlots.setChanged();
                }
                ci.cancel();
            }
        }

        public ItemStack repairTransition(ItemStack stack, IGrindstoneMenu menu) {
            if (menu.isSplitLevelMode()) {
                Object2IntMap.Entry<Holder<Enchantment>> entry =
                    menu.getEnchantments(stack).entrySet().iterator().next();
                EnchantmentHelper.updateEnchantments(stack, mutable ->
                    mutable.set(entry.getKey(), entry.getIntValue() - 1)
                );
            } else {
                boolean isEnchantedBook = stack.is(Items.ENCHANTED_BOOK);
                EnchantmentHelper.updateEnchantments(stack, mutable -> {
                    for (Object2IntMap.Entry<Holder<Enchantment>> entry : menu.getEnchantments(stack).entrySet()) {
                        if (!menu.canTransferCurses() && entry.getKey().is(EnchantmentTags.CURSE)) {
                            continue;
                        }
                        mutable.set(entry.getKey(), 0);
                        if (isEnchantedBook) {
                            break;
                        }
                    }
                });
            }

            int currentRepairCost = stack.getOrDefault(DataComponents.REPAIR_COST, 0);
            stack.set(DataComponents.REPAIR_COST, IGrindstoneMenu.calculateDecreasedRepairCost(currentRepairCost));

            return stack;
        }
    }
}
