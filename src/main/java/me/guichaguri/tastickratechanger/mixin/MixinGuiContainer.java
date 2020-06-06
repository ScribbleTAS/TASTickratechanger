package me.guichaguri.tastickratechanger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tastickratechanger.TickrateChanger;
import me.guichaguri.tastickratechanger.TickrateContainer;
import me.guichaguri.tastickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;

@Mixin(GuiContainer.class)
public abstract class MixinGuiContainer extends GuiScreen {

	@Shadow
	private Slot hoveredSlot;

	@Inject(method="keyTyped", at=@At("HEAD"), cancellable = true)
	public void redoKeyTyped(char typedChar, int keyCode, CallbackInfo ci) {
		if(TickrateChanger.TICKS_PER_SECOND!=0) {
		if (keyCode == 1 || this.mc.gameSettings.keyBindInventory.isActiveAndMatches(keyCode))
        {
            this.mc.player.closeScreen();
        }
		
        this.checkHotbarKeys(keyCode);

        if (this.hoveredSlot != null && this.hoveredSlot.getHasStack())
        {
            if (this.mc.gameSettings.keyBindPickBlock.isActiveAndMatches(keyCode))
            {
                this.handleMouseClick(this.hoveredSlot, this.hoveredSlot.slotNumber, 0, ClickType.CLONE);
            }
            else if (this.mc.gameSettings.keyBindDrop.isActiveAndMatches(keyCode))
            {
                this.handleMouseClick(this.hoveredSlot, this.hoveredSlot.slotNumber, isCtrlKeyDown() ? 1 : 0, ClickType.THROW);
            }
        }
		}
        ci.cancel();
	}
	@Shadow
	protected abstract void handleMouseClick(Slot hoveredSlot2, int slotNumber, int i, ClickType clone);
	@Shadow
	protected abstract boolean checkHotbarKeys(int keyCode2);
}
