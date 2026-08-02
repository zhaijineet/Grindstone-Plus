package net.zhaiji.grindstoneplus.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.GrindstoneEvent;
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

import java.util.LinkedHashMap;
import java.util.Map;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin extends AbstractContainerMenu implements IGrindstoneMenu {
    @Shadow
    public Container repairSlots;
    @Unique
    private int cost;
    @Unique
    private boolean isModResult;
    @Unique
    private boolean wasExtractMode;
    @Unique
    private ItemStack lastInputItem;
    @Shadow
    private int xp;
    @Shadow
    @Final
    private Container resultSlots;

    protected GrindstoneMenuMixin(@Nullable MenuType<?> pMenuType, int pContainerId) {
        super(pMenuType, pContainerId);
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
            if (MinecraftForge.EVENT_BUS.post(event)) {
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

        Map<Enchantment, Integer> enchantments = getEnchantments(itemStack1);
        if (enchantments.isEmpty()) return false;

        // 检查是否有可提取的附魔（非诅咒，或允许转移诅咒时所有附魔）
        boolean hasExtractable = canTransferCurses();
        if (!hasExtractable) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                if (!entry.getKey().isCurse()) {
                    hasExtractable = true;
                    break;
                }
            }
        }
        if (!hasExtractable) return false;

        boolean isEnchantedBook = itemStack1.is(Items.ENCHANTED_BOOK);
        // 提取/拆附魔模式：非附魔书，或附魔书有 >1 个附魔
        boolean canExtract = enchantments.size() > 1 || !isEnchantedBook;
        // 拆等级模式：附魔书只有 1 个附魔且等级 > 1
        boolean canSplitLevel = isEnchantedBook && enchantments.size() == 1 && enchantments.values().iterator().next() > 1;

        return canExtract || canSplitLevel;
    }

    @Override
    public Map<Enchantment, Integer> getEnchantments(ItemStack stack) {
        return EnchantmentHelper.getEnchantments(stack);
    }

    @Override
    public ItemStack createResultItem(ItemStack stack) {
        ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
        boolean isEnchantedBook = stack.is(Items.ENCHANTED_BOOK);

        if (isSplitLevelMode()) {
            Map<Enchantment, Integer> enchantments = getEnchantments(stack);
            Map.Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
            EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(entry.getKey(), entry.getValue() - 1));
        } else {
            for (Map.Entry<Enchantment, Integer> entry : getEnchantments(stack).entrySet()) {
                if (!canTransferCurses() && entry.getKey().isCurse()) {
                    continue;
                }
                EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentInstance(entry.getKey(), entry.getValue()));
                if (isEnchantedBook) {
                    break;
                }
            }
        }

        enchantedBook.setRepairCost(IGrindstoneMenu.calculateDecreasedRepairCost(stack.getBaseRepairCost()));

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
        Map<Enchantment, Integer> enchantments = getEnchantments(itemStack1);
        if (enchantments.size() != 1) return false;
        return enchantments.values().iterator().next() > 1;
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
            if (!player.getAbilities().instabuild && needCost()) {
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
            if (player.getAbilities().instabuild || !menuInterface.needCost()) return true;
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
                if (!MinecraftForge.EVENT_BUS.post(new GrindstoneEvent.OnTakeItem(itemStack1, itemStack2, 0))) {
                    menu.access.execute((level, pos) -> {
                        level.levelEvent(1042, pos, 0);
                    });
                    if (menuInterface.isModResult()) {
                        if (!player.getAbilities().instabuild && menuInterface.needCost()) {
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
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                Map.Entry<Enchantment, Integer> entry = enchantments.entrySet().iterator().next();
                enchantments.put(entry.getKey(), entry.getValue() - 1);
                removeEnchantments(stack);
                EnchantmentHelper.setEnchantments(enchantments, stack);
            } else {
                boolean isEnchantedBook = stack.is(Items.ENCHANTED_BOOK);
                Map<Enchantment, Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
                Map<Enchantment, Integer> tempEnchantments = new LinkedHashMap<>(enchantments);
                for (Map.Entry<Enchantment, Integer> entry : tempEnchantments.entrySet()) {
                    if (!menu.canTransferCurses() && entry.getKey().isCurse()) {
                        continue;
                    }
                    enchantments.remove(entry.getKey());
                    if (isEnchantedBook) {
                        break;
                    }
                }
                removeEnchantments(stack);
                EnchantmentHelper.setEnchantments(enchantments, stack);
            }

            stack.setRepairCost(IGrindstoneMenu.calculateDecreasedRepairCost(stack.getBaseRepairCost()));

            return stack;
        }

        public void removeEnchantments(ItemStack stack) {
            if (stack.is(Items.ENCHANTED_BOOK)) {
                stack.removeTagKey("StoredEnchantments");
            } else {
                stack.removeTagKey("Enchantments");
            }
        }
    }
}
