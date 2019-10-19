package me.guichaguri.tickratechanger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tickratechanger.TickrateChanger;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	@Shadow 
	public abstract void runTickKeyboard();
	@Shadow 
	public abstract void runTickMouse();
	
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
