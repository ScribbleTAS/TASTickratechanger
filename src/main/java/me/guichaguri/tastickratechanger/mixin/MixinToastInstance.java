package me.guichaguri.tastickratechanger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import me.guichaguri.tastickratechanger.TickrateChanger;
import net.minecraft.client.gui.toasts.IToast;

@Mixin(targets="net/minecraft/client/gui/toasts/GuiToast$ToastInstance")
public abstract class MixinToastInstance{

	@ModifyVariable(method="render(II)Z", at=@At(value="STORE", ordinal=0))
	public long redoRender(long i) {
		i=(long) (i*(TickrateChanger.TICKS_PER_SECOND/20));
		return i;
	}
}
