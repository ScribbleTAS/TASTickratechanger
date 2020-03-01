package me.guichaguri.tastickratechanger.mixin;

import java.util.Deque;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.google.common.collect.Queues;

import me.guichaguri.tastickratechanger.TickrateChanger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.GuiToast.ToastInstance;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;

@Mixin(targets="net/minecraft/client/gui/toasts/GuiToast$ToastInstance")
public abstract class MixinToastInstance<T extends IToast> extends Object{

	@ModifyVariable(method="render", at=@At(value="STORE",ordinal = 0))
	public long redoRender(long i,int p_193684_1_, int p_193684_2_) {
		i=(long) (i*(TickrateChanger.TICKS_PER_SECOND/20));
		return i;
	}
}
