package me.guichaguri.tastickratechanger.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tastickratechanger.TickrateChanger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

/**
 * Changes the enchanting glimmer animation to be dependent on the tickrate and the pause menu.<br>
 * Pauses the animation when it's tickrate 0 or the IngameMenu is open<br>
 * <br>
 * From my understanding, the texture is projected onto the item in question, then get's scaled, moved (with GlStateManager.translate()) and finally rotated <br>
 * <br>
 * To animate it, Mojang seems use Minecraft.getSystemTime() % (3000L*multiplier) / (3000.0F*multiplier) / 8.0F to move the texture further up as time progresses.<br>
 * <br>
 * To make it dependent on the tickrate I used a multiplier, which is 20/ClientTickrate, to modify the modulo constant... <br>
 * Not only does this slow down the Animation, but makes it seamless...<br>
 * <br>
 * To pause the animation, I took the current system time calculation from above and saved it in the field save1 or save2 for the other variable. <br>
 * Now in tickrate 0 or in the pause menu, I use that as values for GlStateManager.translate().<br>
 * <br>
 * To resume the animation I use offset1 and offset2. This subtracted from the original calculation, after the tickrate is resumed, puts the position to it's original state without any gaps.<br>
 * <br>
 * And lastMultiplier is used to alter offset and saved depending on the tickrate, once is used to only run code once on tickrate 0
 *
 * @author ScribbleLP
 *
 */
@Mixin(RenderItem.class)
public abstract class MixinRenderItem {
	@Shadow
	TextureManager textureManager;
	@Shadow
	static ResourceLocation RES_ITEM_GLINT;
	@Shadow
	public abstract void renderModel(IBakedModel model, int color);
	
	private Minecraft mc= Minecraft.getMinecraft();
	
	private float saved1=0;
	private float saved2=0;
	private float offset1=0;
	private float offset2=0;
	private float lastMultiplier;
	private boolean once=false;

	/**
	 * Used for calculating offsets, generally for unpausing the games
	 * @param multiplier
	 * @return
	 */
	private float saveOffset(float multiplier) {
		multiplier=(20/TickrateChanger.TICKS_PER_SECOND);
		lastMultiplier=multiplier;
		if(once) {
			offset1=(Minecraft.getSystemTime() % (3000L*lastMultiplier) / (3000.0F*lastMultiplier) / 8.0F)-saved1;
			offset2=(Minecraft.getSystemTime() % (4873L*lastMultiplier) / (4873.0F*lastMultiplier) / 8.0F)-saved2;
			once=false;
		}
		return multiplier;
	}
	/**
	 * Used for saving the position, for pausing the game
	 * @param multiplier
	 * @return
	 */
	private float savePausedValues(float multiplier) {
		multiplier=0F;
		if(!once) {
			saved1=(Minecraft.getSystemTime() % (3000L*lastMultiplier) / (3000.0F*lastMultiplier) / 8.0F)-offset1;
			saved2=(Minecraft.getSystemTime() % (4873L*lastMultiplier) / (4873.0F*lastMultiplier) / 8.0F)-offset2;
			once=true;
		}
		return multiplier;
	}
	
	@Inject(method = "renderEffect", at=@At("HEAD"), cancellable=true)
	public void redorenderEffect(IBakedModel model, CallbackInfo ci) {
		/**
		 * Is used for changing the scroll amount of the RES_ITEM_GLINT texture, so it fits seamlessly into the next "cycle"
		 */
		float multiplier=0F;
		
		if(TickrateChanger.TICKS_PER_SECOND!=0) {
			multiplier=saveOffset(multiplier);
		}else if(TickrateChanger.TICKS_PER_SECOND==0) {
			multiplier=savePausedValues(multiplier);
		}
		if(mc.currentScreen instanceof GuiIngameMenu&&TickrateChanger.TICKS_PER_SECOND!=0) {
			multiplier=savePausedValues(multiplier);
		}
		GlStateManager.depthMask(false);
        GlStateManager.depthFunc(514);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
        this.textureManager.bindTexture(RES_ITEM_GLINT);
        GlStateManager.matrixMode(5890);
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f = (float)(Minecraft.getSystemTime() % (3000L*multiplier) / (3000.0F*multiplier) / 8.0F);
        f=f-offset1;
        if(TickrateChanger.TICKS_PER_SECOND==0 || (mc.currentScreen instanceof GuiIngameMenu &&TickrateChanger.TICKS_PER_SECOND!=0)) {
        	f=saved1;
        }
        GlStateManager.translate(f, 0.0F, 0.0F);
        GlStateManager.rotate(-50.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, -8372020);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(8.0F, 8.0F, 8.0F);
        float f1 = (float)(Minecraft.getSystemTime() % (4873L*multiplier) / (4873.0F*multiplier) / 8.0F);
        f1=f1-offset2;
        if(TickrateChanger.TICKS_PER_SECOND==0 || (mc.currentScreen instanceof GuiIngameMenu &&TickrateChanger.TICKS_PER_SECOND!=0) ) {
        	f1=saved2;
        }
        GlStateManager.translate(-f1, 0.0F, 0.0F);
        GlStateManager.rotate(10.0F, 0.0F, 0.0F, 1.0F);
        this.renderModel(model, -8372020);
        GlStateManager.popMatrix();
        GlStateManager.matrixMode(5888);
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableLighting();
        GlStateManager.depthFunc(515);
        GlStateManager.depthMask(true);
        this.textureManager.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
        ci.cancel();
	}
	

}
