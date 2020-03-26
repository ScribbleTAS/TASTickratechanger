package me.guichaguri.tastickratechanger.mixin;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.Lists;

import me.guichaguri.tastickratechanger.TickrateChanger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.ISoundEventListener;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiSubtitleOverlay;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mixin(GuiSubtitleOverlay.class)
public class MixinGuiSubtitleOverlay extends Gui implements ISoundEventListener{
	@Shadow
	private boolean enabled;
	@Shadow
	private Minecraft client;
	
	private List<MixinGuiSubtitleOverlay.Subtitle> subtitles=Lists.<MixinGuiSubtitleOverlay.Subtitle>newArrayList();
	
	private boolean once=false;
	private float multipliersave=0;

	/**
	 * This was one of the weirdest things to modify while keeping very close to vanilla<br>
	 * <br>
	 * What essentially happens is when a sound plays, a subtitle is created in playSound(). After a while the color of the text darkens until the subtitle disappears <br>
	 * Just like the RenderItem and GuiToast stuff, Minecraft.getSystemTime is used.<br>
	 * <br>
	 * To make it work with the tickratechanger, you just have to multiply the 3000L with the current gamespeed, which is 20/tickrate and watch out that you don't set tickrate to 0<br>
	 * <br>
	 * Again, making tickrate 0 work is a bit more difficult... An offset is used to still use the Minecraft.getSystemTime... And when it's tickrate 0 it saves the current value and applies it continuesly<br>
	 * <br>
	 * And to make that work for every sybtitle on screen there is the custom class Subtitles which holds more values than the vanilla Subtitles... <br>
	 * <br>
	 * If I look at this in half a year I will have no idea what I was doing...
	 */
	@Inject(method="renderSubtitles", at= @At("HEAD"), cancellable= true)
	public void redoRenderSubtitles(ScaledResolution resolution, CallbackInfo ci){
		//Make a working multiplier
		if(TickrateChanger.TICKS_PER_SECOND!=0) {
			multipliersave=20/TickrateChanger.TICKS_PER_SECOND;
		}
		
		if (!this.enabled && this.client.gameSettings.showSubtitles)
        {
            this.client.getSoundHandler().addListener((ISoundEventListener) this);
            this.enabled = true;
        }
        else if (this.enabled && !this.client.gameSettings.showSubtitles)
        {
            this.client.getSoundHandler().removeListener((ISoundEventListener) this);
            this.enabled = false;
        }

        if (this.enabled && !this.subtitles.isEmpty())
        {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            Vec3d vec3d = new Vec3d(this.client.player.posX, this.client.player.posY + (double)this.client.player.getEyeHeight(), this.client.player.posZ);
            Vec3d vec3d1 = (new Vec3d(0.0D, 0.0D, -1.0D)).rotatePitch(-this.client.player.rotationPitch * 0.017453292F).rotateYaw(-this.client.player.rotationYaw * 0.017453292F);
            Vec3d vec3d2 = (new Vec3d(0.0D, 1.0D, 0.0D)).rotatePitch(-this.client.player.rotationPitch * 0.017453292F).rotateYaw(-this.client.player.rotationYaw * 0.017453292F);
            Vec3d vec3d3 = vec3d1.crossProduct(vec3d2);
            int i = 0;
            int j = 0;
            Iterator<MixinGuiSubtitleOverlay.Subtitle> iterator = this.subtitles.iterator();

            while (iterator.hasNext())
            {
                MixinGuiSubtitleOverlay.Subtitle guisubtitleoverlay$subtitle = iterator.next();
                //Set and reset offset and multiplier
                float multiplier;
                if(TickrateChanger.TICKS_PER_SECOND!=0) {
                	multiplier=multipliersave;
                	if(guisubtitleoverlay$subtitle.isOnce()) {
                    	guisubtitleoverlay$subtitle.setOffset();
                    	guisubtitleoverlay$subtitle.setOnce(false);
                	}
                }else {
                	multiplier=Float.MAX_VALUE;
                	if(!guisubtitleoverlay$subtitle.isOnce()) {
                		guisubtitleoverlay$subtitle.setSave();
                	}
                }
                //Time until the subtitle disappears is altered here. The default is 3000L
                if (guisubtitleoverlay$subtitle.getStartTime() + (3000L*multiplier) <= (Minecraft.getSystemTime()-guisubtitleoverlay$subtitle.getOffset()))
                {
                    iterator.remove();
                }
                else
                {
                    j = Math.max(j, this.client.fontRenderer.getStringWidth(guisubtitleoverlay$subtitle.getString()));
                }
            }

            j = j + this.client.fontRenderer.getStringWidth("<") + this.client.fontRenderer.getStringWidth(" ") + this.client.fontRenderer.getStringWidth(">") + this.client.fontRenderer.getStringWidth(" ");
            
            for (MixinGuiSubtitleOverlay.Subtitle guisubtitleoverlay$subtitle1 : this.subtitles)
            {
                int k = 255;
                String s = guisubtitleoverlay$subtitle1.getString();
                Vec3d vec3d4 = guisubtitleoverlay$subtitle1.getLocation().subtract(vec3d).normalize();
                double d0 = -vec3d3.dotProduct(vec3d4);
                double d1 = -vec3d1.dotProduct(vec3d4);
                boolean flag = d1 > 0.5D;
                int l = j / 2;
                int i1 = this.client.fontRenderer.FONT_HEIGHT;
                int j1 = i1 / 2;
                float f = 1.0F;
                int k1 = this.client.fontRenderer.getStringWidth(s);
                if(TickrateChanger.TICKS_PER_SECOND!=0) {
                	if(guisubtitleoverlay$subtitle1.isOnce()) {
                    	guisubtitleoverlay$subtitle1.setOffset();
                    	guisubtitleoverlay$subtitle1.setOnce(false);
                	}
                }else {
                	if(!guisubtitleoverlay$subtitle1.isOnce()) {	//Executed when the tickrate is 0
                		guisubtitleoverlay$subtitle1.setSave();
                	}
                }
                int l1 = MathHelper.floor(MathHelper.clampedLerp(255.0D, 75.0D, (double)((float)((Minecraft.getSystemTime()-guisubtitleoverlay$subtitle1.getOffset()) - guisubtitleoverlay$subtitle1.getStartTime()) / (3000.0F*multipliersave))));
                int i2 = l1 << 16 | l1 << 8 | l1;
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)resolution.getScaledWidth() - (float)l * 1.0F - 2.0F, (float)(resolution.getScaledHeight() - 30) - (float)(i * (i1 + 1)) * 1.0F, 0.0F);
                GlStateManager.scale(1.0F, 1.0F, 1.0F);
                drawRect(-l - 1, -j1 - 1, l + 1, j1 + 1, -872415232);
                if(TickrateChanger.TICKS_PER_SECOND==0) {
                	if(!guisubtitleoverlay$subtitle1.isOnce()) {
                    	guisubtitleoverlay$subtitle1.setPauseval(i2);
                    	guisubtitleoverlay$subtitle1.setOnce(true);
                	}else {
                		i2=guisubtitleoverlay$subtitle1.getPauseval();
                	}
                }
                GlStateManager.enableBlend();
                if (!flag)
                {
                    if (d0 > 0.0D)
                    {
                        this.client.fontRenderer.drawString(">", l - this.client.fontRenderer.getStringWidth(">"), -j1, i2 + -16777216);
                    }
                    else if (d0 < 0.0D)
                    {
                        this.client.fontRenderer.drawString("<", -l, -j1, i2 + -16777216);
                    }
                }

                this.client.fontRenderer.drawString(s, -k1 / 2, -j1, i2 + -16777216);
                GlStateManager.popMatrix();
                ++i;
            }

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
        ci.cancel();
	}

	@Override
	public void soundPlay(ISound soundIn, SoundEventAccessor accessor) {
		if (accessor.getSubtitle() != null)
        {
            String s = accessor.getSubtitle().getFormattedText();

            if (!this.subtitles.isEmpty())
            {
                for (MixinGuiSubtitleOverlay.Subtitle guisubtitleoverlay$subtitle : this.subtitles)
                {
                    if (guisubtitleoverlay$subtitle.getString().equals(s))
                    {
                        guisubtitleoverlay$subtitle.refresh(new Vec3d((double)soundIn.getXPosF(), (double)soundIn.getYPosF(), (double)soundIn.getZPosF()));
                        return;
                    }
                }
            }

            this.subtitles.add(new MixinGuiSubtitleOverlay.Subtitle(s, new Vec3d((double)soundIn.getXPosF(), (double)soundIn.getYPosF(), (double)soundIn.getZPosF())));
        }
	}

	@SideOnly(Side.CLIENT)
	public class Subtitle {
		private final String subtitle;
		private long startTime;
		private Vec3d location;
		private int pauseval;
		private long offset;
		private long save;
		private boolean once;

		public Subtitle(String subtitleIn, Vec3d locationIn) {
			this.subtitle = subtitleIn;
			this.location = locationIn;
			this.offset=0;
			this.save=0;
			this.startTime = Minecraft.getSystemTime()-offset;
			this.once=false;
		}

		public String getString() {
			return this.subtitle;
		}

		public long getStartTime() {
			return this.startTime;
		}

		public Vec3d getLocation() {
			return this.location;
		}

		public void refresh(Vec3d locationIn) {
			this.location = locationIn;
			this.startTime = Minecraft.getSystemTime()-offset;
		}
		public void setPauseval(int val) {
			this.pauseval=val;
		}
		public int getPauseval() {
			return pauseval;
		}
		public void setOffset() {
			this.offset=Minecraft.getSystemTime()-save;
		}
		public long getOffset() {
			return this.offset;
		}
		public void setSave() {
			this.save = Minecraft.getSystemTime()-offset;
		}
		public long getSave() {
			return save;
		}
		public void setOnce(boolean once) {
			this.once = once;
		}
		public boolean isOnce() {
			return once;
		}
	}
}