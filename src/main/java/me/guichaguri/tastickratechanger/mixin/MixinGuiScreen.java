package me.guichaguri.tastickratechanger.mixin;

import java.io.IOException;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tastickratechanger.TickrateChanger;
import me.guichaguri.tastickratechanger.TickrateContainer;
import me.guichaguri.tastickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

@Mixin(GuiScreen.class)
public abstract class MixinGuiScreen {
	@Shadow
	private boolean mouseHandled;
	@Shadow
	private boolean keyHandled;
	@Shadow
	private Minecraft mc;

	@SuppressWarnings("unlikely-arg-type")
	@Inject(method="handleInput",at=@At("HEAD"), cancellable = true)
	public void redohandleInput(CallbackInfo ci) throws IOException
	{
		if (Mouse.isCreated())
        {
            while (Mouse.next())
            {
            	this.mouseHandled = false;
            	if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Pre((GuiScreen)(Object)this))) continue;
	            this.handleMouseInput();
	            if (this.equals(this.mc.currentScreen) && !this.mouseHandled) net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.MouseInputEvent.Post((GuiScreen)(Object)this));
			}
        }

        if (Keyboard.isCreated())
        {
            while (Keyboard.next())
            {
                this.keyHandled = false;
                if (net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Pre((GuiScreen)(Object)this))) continue;
                this.handleKeyboardInput();
                if (this.equals(this.mc.currentScreen) && !this.keyHandled) net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(new net.minecraftforge.client.event.GuiScreenEvent.KeyboardInputEvent.Post((GuiScreen)(Object)this));
            }
        }
        ci.cancel();
	}
	@Shadow
	protected abstract void handleKeyboardInput();
	@Shadow
	public abstract void handleMouseInput();

}
