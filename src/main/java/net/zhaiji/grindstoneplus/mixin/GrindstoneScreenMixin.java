package net.zhaiji.grindstoneplus.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.GrindstoneScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.GrindstoneMenu;
import net.zhaiji.grindstoneplus.GrindstoneMenuInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GrindstoneScreen.class)
public abstract class GrindstoneScreenMixin extends AbstractContainerScreen<GrindstoneMenu> {
    public Player player;

    public GrindstoneScreenMixin(GrindstoneMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void grindstone_Plus$init(GrindstoneMenu menu, Inventory playerInventory, Component title, CallbackInfo ci) {
        this.player = playerInventory.player;
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        GrindstoneMenuInterface menuInterface = (GrindstoneMenuInterface) this.menu;
        if (!menuInterface.needCost()) return;
        int cost = menuInterface.getCost();
        int textColor = 8453920;
        Component component = null;
        if (menuInterface.canCreateResult()) {
            component = Component.translatable("container.repair.cost", cost);
            if (!this.menu.getSlot(2).mayPickup(this.player)) {
                textColor = 16736352;
            }
        }
        if (component != null) {
            int x = this.imageWidth - 8 - this.font.width(component) - 2;
            int y = 69;
            guiGraphics.fill(x - 2, y - 2, this.imageWidth - 8, y + 10, 1325400064);
            guiGraphics.drawString(this.font, component, x, y, textColor);
        }
    }
}
