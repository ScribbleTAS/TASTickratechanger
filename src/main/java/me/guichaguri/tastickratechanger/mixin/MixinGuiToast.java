package me.guichaguri.tastickratechanger.mixin;

import java.util.Deque;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import com.google.common.collect.Queues;

import me.guichaguri.tastickratechanger.TickrateChanger;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.gui.toasts.IToast.Visibility;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mixin(GuiToast.class)
public abstract class MixinGuiToast{
	
	private MixinGuiToast.ToastInstance<?>[] visibles = new MixinGuiToast.ToastInstance[5];
	private Minecraft mc=Minecraft.getMinecraft();
	@Shadow
	private Deque<IToast> toastsQueue;
	
	/**
	 * This mixin tries to make the animation of the advancement toasts dependent on the tickrate while keeping the code as vanilla as possible<br>
	 * <br>
	 * While I spent a long amount of time watching this code, I still don't quite fully understand the math behind this...<br>
	 * <br>
	 * Here's what I could find out: Toasts have 2 different states represented in visibility in the ToastInstance.<br>
	 * And if it's set to SHOW the fly in animation and sound will play, the same goes with HIDE where it flies out after a certain amount of time<br>
	 * After a lot of trial and error I found out that animationTimer, which was originally "i", is the way to go...<br>
	 * <br>
	 * So just as RenderItem and GuiSubtitleOverlay, things are done with an offset for tickrate 0 and simple multiplication<br>
	 * Also I used a copy of the vanilla ToastInstance-class to make it work for every subtitle on screen... If you seek to make it work for only 1, at the end is a commented code that shows you how to use @ModyfyVarable<br>
	 * <br>
	 * There is one compromise I had to make... When you change the tickrate while a toast is showing, it will stay at the old tickrate until it's done...<br>
	 * Maybe I still fix this and get into this mess once more, but for now this will do and don't make the subtitles stuck in a loop until you change to the old tickrate<br>
     * Am I doing this right with commenting code? I hope so...<br>
     * @author ScribbleLP
	 */
	@Inject(method="drawToast", at=@At("HEAD"), cancellable=true)
	public void redoDrawToast(ScaledResolution resolution, CallbackInfo ci)
    {
        if (!this.mc.gameSettings.hideGUI)
        {
            RenderHelper.disableStandardItemLighting();

            for (int i = 0; i < this.visibles.length; ++i)
            {
                MixinGuiToast.ToastInstance<?> toastinstance = this.visibles[i];

                if (toastinstance != null && toastinstance.render(resolution.getScaledWidth(), i))
                {
                    this.visibles[i] = null;
                }

                if (this.visibles[i] == null && !this.toastsQueue.isEmpty())
                {
                    this.visibles[i] = new MixinGuiToast.ToastInstance(this.toastsQueue.removeFirst());
                }
            }
        }
        ci.cancel();
    }
	@SideOnly(Side.CLIENT)
    class ToastInstance<T extends IToast>
    {
		//Values to store the animation timer
		private long store=0L;
		private boolean once=false;
		private long offset=0L;
		private long offsetting=0L;
		private float ticksave=TickrateChanger.TICKS_PER_SECOND;
		
        public final T toast;
        public long animationTime;
        public long visibleTime;
        public IToast.Visibility visibility;

        private ToastInstance(T toastIn)
        {
            this.animationTime = -1L;
            this.visibleTime = -1L;
            this.visibility = IToast.Visibility.SHOW;
            this.toast = toastIn;
        }

        public T getToast()
        {
            return this.toast;
        }

        private float getVisibility(long scaledresolutionWidth)
        {
            float f = MathHelper.clamp((float)(scaledresolutionWidth - this.animationTime) / 600.0F, 0.0F, 1.0F);
            f = f * f;
            return this.visibility == IToast.Visibility.HIDE ? 1.0F - f : f;
        }

        public boolean render(int scaledresolutionWidth, int scaledresolutionHeight)
        {
        	long animationTimer=0L;
        	if(TickrateChanger.TICKS_PER_SECOND!=0) {
    			if(once) {
    				once=false;
    				offset=Minecraft.getSystemTime()-store;
    			}
    			animationTimer= (long)((Minecraft.getSystemTime()-offset)*(ticksave/20));
    		}else{
    			if(!once) {
    				once=true;
    				store=(long) ((Minecraft.getSystemTime()-offset));
    			}
    			animationTimer=(long) (store*(ticksave/20));
    		}

            if (this.animationTime == -1L)
            {
                this.animationTime = animationTimer;
                this.visibility.playSound(mc.getSoundHandler());
            }

            if (this.visibility == IToast.Visibility.SHOW && animationTimer - this.animationTime <= 600L)
            {
                this.visibleTime = animationTimer;
            }

            GlStateManager.pushMatrix();
            GlStateManager.translate((float)scaledresolutionWidth - 160.0F * this.getVisibility(animationTimer), (float)(scaledresolutionHeight * 32), (float)(500 + scaledresolutionHeight));
            IToast.Visibility itoast$visibility = this.toast.draw(new GuiToast(mc), animationTimer - this.visibleTime);
            GlStateManager.popMatrix();

            if (itoast$visibility != this.visibility)
            {
                this.animationTime = animationTimer - (long)((int)((1.0F - this.getVisibility(animationTimer)) * 600.0F));
                this.visibility = itoast$visibility;
                this.visibility.playSound(mc.getSoundHandler());
            }

            return this.visibility == IToast.Visibility.HIDE && animationTimer - this.animationTime > 600L;
        }
    }
}

/* This was the original mixin for GuiToast$ToastInstance... Now this is only for my reference here, if I ever have to do something with Modify variable again
 * 
@ModifyVariable(method="render(II)Z", at=@At(value="STORE", ordinal=0))
public long redoRender(long i) {
	if(TickrateChanger.TICKS_PER_SECOND!=0) {
		if(once) {
			once=false;
			offset=Minecraft.getSystemTime()-store;
		}
		i= (long)((Minecraft.getSystemTime()-offset)*(TickrateChanger.TICKS_PER_SECOND/20));
	}else {
		if(!once) {
			once=true;
			store=(long) ((Minecraft.getSystemTime()-offset)*(TickrateChanger.TICKRATE_SAVED/20));
		}
		i=store;
	}
	return i;
}*/
