package me.guichaguri.tastickratechanger.mixin;

import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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
	
	private List<MixinGuiSubtitleOverlay.Subtitle> subtitles;
	
	private boolean once=false;
	private long save=0;
	private long offset=0;

	@Inject(method="renderSubtitles", at= @At("HEAD"), cancellable= true)
	public void redoRenderSubtitles(ScaledResolution resolution, CallbackInfo ci){
		//Make a working multiplier
		float multiplier;
		if(TickrateChanger.TICKS_PER_SECOND!=0) {
			multiplier=20/TickrateChanger.TICKS_PER_SECOND;
			if(once==true) {
				once=false;
				offset=Minecraft.getSystemTime()-save;
			}
		}else {
			multiplier=Float.MAX_VALUE;
			if(once==false) {
				save=Minecraft.getSystemTime();
			}
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

                if ((guisubtitleoverlay$subtitle.getStartTime()+offset) + (3000L*multiplier) <= Minecraft.getSystemTime())
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
                int l1 = MathHelper.floor(MathHelper.clampedLerp(255.0D, 75.0D, (double)((float)(Minecraft.getSystemTime() - (guisubtitleoverlay$subtitle1.getStartTime()+offset)) / (3000.0F*multiplier))));
                int i2 = l1 << 16 | l1 << 8 | l1;
                GlStateManager.pushMatrix();
                GlStateManager.translate((float)resolution.getScaledWidth() - (float)l * 1.0F - 2.0F, (float)(resolution.getScaledHeight() - 30) - (float)(i * (i1 + 1)) * 1.0F, 0.0F);
                GlStateManager.scale(1.0F, 1.0F, 1.0F);
                drawRect(-l - 1, -j1 - 1, l + 1, j1 + 1, -872415232);
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

		public Subtitle(String subtitleIn, Vec3d locationIn) {
			this.subtitle = subtitleIn;
			this.location = locationIn;
			this.startTime = Minecraft.getSystemTime();
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
			this.startTime = Minecraft.getSystemTime();
		}
	}
}
