package me.guichaguri.tastickratechanger.mixin;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.FutureTask;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tastickratechanger.TASTickEvent;
import me.guichaguri.tastickratechanger.TickrateChanger;
import me.guichaguri.tastickratechanger.TickrateContainer;
import me.guichaguri.tastickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.entity.Entity;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraftforge.common.MinecraftForge;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	Minecraft mc= Minecraft.getMinecraft();
	@Shadow
	private SoundHandler mcSoundHandler;
	
    @Shadow
	GuiScreen currentScreen;
    @Shadow
    RenderGlobal renderGlobal;
    @Shadow
    GameSettings gameSettings;
    @Shadow
    EntityRenderer entityRenderer;
    @Shadow
    public abstract Entity getRenderViewEntity();
    @Shadow
    EntityPlayerSP player;
    @Shadow
    GuiIngame ingameGUI;
    @Shadow
    PlayerControllerMP playerController;
    @Shadow
    Tutorial tutorial;
    @Shadow
    public abstract void displayGuiScreen(@Nullable GuiScreen guiScreenIn);
    @Shadow
    public abstract NetHandlerPlayClient getConnection();
    @Shadow
    public abstract void clickMouse();
    @Shadow
    public abstract void rightClickMouse();
    @Shadow
    public abstract void middleClickMouse();
    @Shadow
    int rightClickDelayTimer;
    @Shadow
    boolean inGameHasFocus;
    @Shadow
	private long debugCrashKeyPressTime;
    @Shadow
	private boolean actionKeyF3;

	@Shadow
	private Profiler mcProfiler;
	@Shadow
	private Timer timer;
	@Shadow
	private Queue < FutureTask<? >> scheduledTasks;
	@Shadow
	private Framebuffer framebufferMc;
	@Shadow
	private boolean skipRenderWorld;
	@Shadow
	private GuiToast toastGui;
	@Shadow
	private boolean isGamePaused;
	@Shadow
	private float renderPartialTicksPaused;
	@Shadow
	private long prevFrameTime;
	@Shadow
	private int displayWidth;
	@Shadow
	private int displayHeight;
	@Shadow
	private int fpsCounter;
	@Shadow
	private FrameTimer frameTimer;
	@Shadow
	private long startNanoTime;
	@Shadow
	private long debugUpdateTime;
	@Shadow
	private String debug;
	@Shadow
	private Snooper usageSnooper;
	@Shadow
	private IntegratedServer integratedServer;
	@Shadow
	private static int debugFPS;

    
    
    @Inject(method = "runGameLoop", at = @At(value="HEAD"), cancellable = true)
    public void redoentireRunGameLoop(CallbackInfo ci) throws IOException {
    	long i = System.nanoTime();
        this.mcProfiler.startSection("root");

        if (Display.isCreated() && Display.isCloseRequested())
        {
            this.shutdown();
        }

        this.timer.updateTimer();
        this.mcProfiler.startSection("scheduledExecutables");

        synchronized (this.scheduledTasks)
        {
            while (!this.scheduledTasks.isEmpty())
            {
                Util.runTask(this.scheduledTasks.poll(), Minecraft.LOGGER);
            }
        }

        this.mcProfiler.endSection();
        long l = System.nanoTime();
        this.mcProfiler.startSection("tick");
        redoRunGameLoopHead();
		for (int j = 0; j < Math.min(10, this.timer.elapsedTicks); ++j)
		{
			this.runTick();
		}
		this.mcProfiler.endStartSection("preRenderErrors");
        long i1 = System.nanoTime() - l;
        this.checkGLError("Pre render");
        this.mcProfiler.endStartSection("sound");
        this.mcSoundHandler.setListener(this.getRenderViewEntity(), this.timer.renderPartialTicks); //Forge: MC-46445 Spectator mode particles and sounds computed from where you have been before
        this.mcProfiler.endSection();
        this.mcProfiler.startSection("render");
        GlStateManager.pushMatrix();
        GlStateManager.clear(16640);
        this.framebufferMc.bindFramebuffer(true);
        this.mcProfiler.startSection("display");
        GlStateManager.enableTexture2D();
        this.mcProfiler.endSection();

        if (!this.skipRenderWorld)
        {
            net.minecraftforge.fml.common.FMLCommonHandler.instance().onRenderTickStart(this.timer.renderPartialTicks);
            this.mcProfiler.endStartSection("gameRenderer");
            this.entityRenderer.updateCameraAndRender(this.isGamePaused ? this.renderPartialTicksPaused : this.timer.renderPartialTicks, i);
            this.mcProfiler.endStartSection("toasts");
            this.toastGui.drawToast(new ScaledResolution((Minecraft)(Object)this));
            this.mcProfiler.endSection();
            net.minecraftforge.fml.common.FMLCommonHandler.instance().onRenderTickEnd(this.timer.renderPartialTicks);
        }

        this.mcProfiler.endSection();

        if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart && !this.gameSettings.hideGUI)
        {
            if (!this.mcProfiler.profilingEnabled)
            {
                this.mcProfiler.clearProfiling();
            }

            this.mcProfiler.profilingEnabled = true;
            this.displayDebugInfo(i1);
        }
        else
        {
            this.mcProfiler.profilingEnabled = false;
            this.prevFrameTime = System.nanoTime();
        }

        this.framebufferMc.unbindFramebuffer();
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        this.entityRenderer.renderStreamIndicator(this.timer.renderPartialTicks);
        GlStateManager.popMatrix();
        this.mcProfiler.startSection("root");
        this.updateDisplay();
        Thread.yield();
        this.checkGLError("Post render");
        ++this.fpsCounter;
        boolean flag = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.integratedServer.getPublic();

        if (this.isGamePaused != flag)
        {
            if (this.isGamePaused)
            {
                this.renderPartialTicksPaused = this.timer.renderPartialTicks;
            }
            else
            {
                this.timer.renderPartialTicks = this.renderPartialTicksPaused;
            }

            this.isGamePaused = flag;
        }

        long k = System.nanoTime();
        this.frameTimer.addFrame(k - this.startNanoTime);
        this.startNanoTime = k;

        while (mc.getSystemTime() >= this.debugUpdateTime + 1000L)
        {
            debugFPS = this.fpsCounter;
            this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated == 1 ? "" : "s", (float)this.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : this.gameSettings.limitFramerate, this.gameSettings.enableVsync ? " vsync" : "", this.gameSettings.fancyGraphics ? "" : " fast", this.gameSettings.clouds == 0 ? "" : (this.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
            RenderChunk.renderChunksUpdated = 0;
            this.debugUpdateTime += 1000L;
            this.fpsCounter = 0;
            this.usageSnooper.addMemoryStatsToSnooper();

            if (!this.usageSnooper.isSnooperRunning())
            {
                this.usageSnooper.startSnooper();
            }
        }

        if (this.isFramerateLimitBelowMax())
        {
            this.mcProfiler.startSection("fpslimit_wait");
            Display.sync(this.getLimitFramerate());
            this.mcProfiler.endSection();
        }

        this.mcProfiler.endSection();
        redoRunGameLoopReturn();
        ci.cancel();
    }
	@Shadow
    protected abstract void displayDebugInfo(long i1);
    @Shadow
	protected abstract void shutdown();
    @Shadow
	protected abstract void runTick();
    @Shadow
	protected abstract boolean isSingleplayer();
    @Shadow
	protected abstract void checkGLError(String string);
    @Shadow
	protected abstract void updateDisplay();
	@Shadow
	protected abstract int getLimitFramerate();
    @Shadow
	protected abstract boolean isFramerateLimitBelowMax();
	
    @Inject(method="runTick", at=@At("HEAD"))
    public void redoRunTick(CallbackInfo ci) {
    	if (TickrateChanger.ADVANCE_TICK==true) {
    		TickrateAPI.handlePausingGame(true);
			TickrateChanger.ADVANCE_TICK=false;
    	}
    }
	private void redoRunGameLoopHead() throws IOException {
		bypass();
		TickrateChanger.TASTIMER.updateTimer();
		for (int j = 0; j < Math.min(10, TickrateChanger.TASTIMER.elapsedTicks); ++j)
        {
            MinecraftForge.EVENT_BUS.post(new TASTickEvent());
        }
		if(TickrateChanger.TICKS_PER_SECOND==0) {
			TickrateChanger.WASZERO=true;
			this.mcSoundHandler.pauseSounds();
		}
	}
	/**
	 * Executed after the RunGameLoopHead function.<br>
	 * Resumes sounds and resets 'WASZERO'
	 */
	private void redoRunGameLoopReturn() {
		if(TickrateChanger.WASZERO&&TickrateChanger.TICKS_PER_SECOND!=0) {
			this.mcSoundHandler.resumeSounds();
		}
		if(TickrateChanger.WASZERO&&TickrateChanger.TICKS_PER_SECOND!=0) {
			TickrateChanger.WASZERO=false;
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
	
    
    private void bypass(){
    	if(TickrateContainer.COOLDOWN_PAUSE_KEY>0) {
			TickrateContainer.COOLDOWN_PAUSE_KEY--;
		}
		if(TickrateContainer.COOLDOWN_ADVANCE_KEY>0) {
			TickrateContainer.COOLDOWN_ADVANCE_KEY--;
		}
    	if(Keyboard.isKeyDown(TickrateContainer.KEYCODE_PAUSE_KEY)&&TickrateContainer.COOLDOWN_PAUSE_KEY==0) {
    		TickrateContainer.COOLDOWN_PAUSE_KEY=10;
    		TickrateAPI.pauseUnpauseGame();
    	}
    	if(Keyboard.isKeyDown(TickrateContainer.KEYCODE_ADVANCE_KEY)&&TickrateContainer.COOLDOWN_ADVANCE_KEY==0) {
    		TickrateContainer.COOLDOWN_ADVANCE_KEY=10;
    		TickrateAPI.advanceTick();
    	}
    }
}
