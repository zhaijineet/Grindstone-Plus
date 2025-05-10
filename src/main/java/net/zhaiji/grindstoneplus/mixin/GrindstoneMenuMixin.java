package net.zhaiji.grindstoneplus.mixin;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.GrindstoneEvent;
import net.zhaiji.grindstoneplus.GrindstoneMenuInterface;
import net.zhaiji.grindstoneplus.GrindstonePlusConfig;
import net.zhaiji.grindstoneplus.compat.TaxFreeLevelsCompat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(GrindstoneMenu.class)
public abstract class GrindstoneMenuMixin extends AbstractContainerMenu implements GrindstoneMenuInterface {
    @Shadow
    public Container repairSlots;
    @Shadow
    @Final
    private Container resultSlots;
    @Shadow
    private int xp;
    public Player player;
    public int cost;

    protected GrindstoneMenuMixin(@Nullable MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
    }

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V", at = @At("RETURN"))
    public void grindstonePlus$init(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        this.player = playerInventory.player;
    }

    @Inject(method = "createResult", at = @At("HEAD"), cancellable = true)
    public void grindstonePlus$createResult(CallbackInfo ci) {
        this.cost = 0;
        if (this.canCreateResult()) {
            ItemStack itemStack1 = this.repairSlots.getItem(0);
            ItemStack itemStack2 = this.repairSlots.getItem(1);
            GrindstoneEvent.OnPlaceItem event = new GrindstoneEvent.OnPlaceItem(itemStack1, itemStack2, this.xp);
            if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
                this.xp = -1;
                this.resultSlots.setItem(0, ItemStack.EMPTY);
            } else if (event.getOutput().isEmpty()) {
                this.xp = Integer.MIN_VALUE;
                this.resultSlots.setItem(0, this.createResultItem(itemStack1));
            } else {
                this.xp = event.getXp();
                this.resultSlots.setItem(0, event.getOutput());
            }
            this.broadcastChanges();
            ci.cancel();
        }
    }

    @Override
    public boolean canCreateResult() {
        ItemStack itemStack1 = this.repairSlots.getItem(0);
        ItemStack itemStack2 = this.repairSlots.getItem(1);
        ItemEnchantments itemEnchantments = this.getEnchantments(itemStack1);
        boolean hasNonCurse = false;
        if (this.canTransferCurses()) {
            hasNonCurse = true;
        } else {
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : itemEnchantments.entrySet()) {
                if (!entry.getKey().is(EnchantmentTags.CURSE)) {
                    hasNonCurse = true;
                    break;
                }
            }
        }
        return (itemStack2.is(Items.BOOK) && hasNonCurse && (!itemEnchantments.isEmpty() || itemStack1.is(Items.ENCHANTED_BOOK)) && (itemEnchantments.size() > 1 || !itemStack1.is(Items.ENCHANTED_BOOK)));
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
        EnchantmentHelper.updateEnchantments(enchantedBook, mutable -> {
            boolean isEnchantedBook = stack.is(Items.ENCHANTED_BOOK);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : this.getEnchantments(stack).entrySet()) {
                if (!this.canTransferCurses() && entry.getKey().is(EnchantmentTags.CURSE)) {
                    continue;
                }
                mutable.set(entry.getKey(), entry.getIntValue());
                if (isEnchantedBook) {
                    break;
                }
            }
        });
        if (GrindstonePlusConfig.costType.equals(GrindstonePlusConfig.CostType.COUNT_COST)) {
            this.cost = this.getEnchantments(enchantedBook).size();
        } else if (GrindstonePlusConfig.costType.equals(GrindstonePlusConfig.CostType.FIXED_COST)) {
            this.cost = GrindstonePlusConfig.fixedCost;
        } else if ((GrindstonePlusConfig.costType.equals(GrindstonePlusConfig.CostType.ANVIL_COST))) {
            for (int index = 0; index < this.getEnchantments(enchantedBook).size(); index++) {
                this.cost = AnvilMenu.calculateIncreasedRepairCost(this.cost);
            }
            if (this.cost > 40) {
                this.cost = 39;
            }
        }
        return enchantedBook;
    }

    @Override
    public boolean needCost() {
        return !GrindstonePlusConfig.costType.equals(GrindstonePlusConfig.CostType.NO_COST);
    }

    @Override
    public int getCost() {
        return this.cost;
    }

    @Override
    public boolean canTransferCurses() {
        return GrindstonePlusConfig.transferCurses;
    }

    @Mixin(targets = "net/minecraft/world/inventory/GrindstoneMenu$3")
    public abstract static class GrindstoneMenu$3Mixin extends Slot {
        public GrindstoneMenu$3Mixin(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
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
            GrindstoneMenuInterface menuInterface = (GrindstoneMenuInterface) player.containerMenu;
            return (player.hasInfiniteMaterials() || !menuInterface.needCost() || player.experienceLevel >= menuInterface.getCost());
        }

        @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
        public void grindstonePlus$onTake(Player player, ItemStack stack, CallbackInfo ci) {
            GrindstoneMenu menu = (GrindstoneMenu) player.containerMenu;
            GrindstoneMenuInterface menuInterface = (GrindstoneMenuInterface) player.containerMenu;
            if (menuInterface.canCreateResult()) {
                ItemStack itemStack1 = menu.repairSlots.getItem(0);
                ItemStack itemStack2 = menu.repairSlots.getItem(1);
                if (!NeoForge.EVENT_BUS.post(new GrindstoneEvent.OnTakeItem(itemStack1, itemStack2, 0)).isCanceled()) {
                    menu.access.execute((level, pos) -> {
                        level.levelEvent(1042, pos, 0);
                    });
                    if (!player.getAbilities().instabuild && menuInterface.needCost()) {
                        if (TaxFreeLevelsCompat.isLoad()) {
                            player.giveExperiencePoints(-TaxFreeLevelsCompat.computeCost(menuInterface.getCost()));
                        }else {
                            player.giveExperienceLevels(-menuInterface.getCost());
                        }
                    }
                    this.repairTransition(itemStack1, menuInterface);
                    itemStack2.shrink(1);
                    menu.repairSlots.setChanged();
                    ci.cancel();
                }
            }
        }

        public ItemStack repairTransition(ItemStack stack, GrindstoneMenuInterface menu) {
            boolean isEnchantedBook = stack.is(Items.ENCHANTED_BOOK);
            ItemEnchantments itemEnchantments = EnchantmentHelper.updateEnchantments(stack, mutable -> {
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
            int j = itemEnchantments.size();
            if (isEnchantedBook) {
                j = Math.max(0, j - 1);
            }
            int i = 0;
            for (int index = 0; index < j; index++) {
                i = AnvilMenu.calculateIncreasedRepairCost(i);
            }
            stack.set(DataComponents.REPAIR_COST, i);
            return stack;
        }
    }
}
