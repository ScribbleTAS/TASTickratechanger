package me.guichaguri.tastickratechanger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tastickratechanger.TickrateChanger;
import paulscode.sound.Source;

@Mixin(Source.class)
public abstract class MixinSource {
	@Shadow(remap=false)
	private float pitch;
	
	/**
	 * A mixin to change the pitch of the sound to be slower or faster than 1.5 times the original speed
	 *
	 * @author ScribbleLP, famous1622
	 */
	@Inject(method=("setPitch"), at=@At("HEAD"), cancellable=true, remap=false)
	  private void onGetPitch(float value, CallbackInfo ci){
		if(TickrateChanger.CHANGE_SOUND) pitch=value*TickrateChanger.GAME_SPEED;
		else pitch=value;
		ci.cancel();
	  }
}
