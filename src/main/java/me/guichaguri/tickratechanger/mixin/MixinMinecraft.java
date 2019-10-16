package me.guichaguri.tickratechanger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tickratechanger.TickrateChanger;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	@Shadow 
	public abstract void runTickKeyboard();
	
	@Inject(method = "runGameLoop", at = @At(value="HEAD"),cancellable = true)
	private void processKeybind(CallbackInfo ci) {
		if(TickrateChanger.TICKS_PER_SECOND==0) {
			this.runTickKeyboard();
		}
	}
}
