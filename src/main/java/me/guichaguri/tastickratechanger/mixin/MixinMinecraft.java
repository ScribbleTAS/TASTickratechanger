package me.guichaguri.tastickratechanger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tastickratechanger.TickrateChanger;
import me.guichaguri.tastickratechanger.TickrateContainer;
import me.guichaguri.tastickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	Minecraft mc= Minecraft.getMinecraft();
	@Shadow 
	public abstract void runTickKeyboard();
	
	@Inject(method = "runGameLoop", at = @At(value="HEAD"),cancellable = true)
	private void processKeybind(CallbackInfo ci) {
		if(TickrateChanger.TICKS_PER_SECOND==0) {
			this.runTickKeyboard();
		}
	}
	@ModifyConstant(method = "runTickMouse", constant= {@Constant(longValue=200L)})
	private long fixMouseWheel(long ignored) {
		if(TickrateChanger.TICKS_PER_SECOND!=0) {
			return (long)Math.max(4000F/TickrateChanger.TICKS_PER_SECOND, 200L);
		}else {
			return 200L;
		}
	}
}
