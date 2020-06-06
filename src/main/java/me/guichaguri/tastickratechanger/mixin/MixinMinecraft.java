package me.guichaguri.tastickratechanger.mixin;

import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.FutureTask;

import javax.annotation.Nullable;

import org.apache.logging.log4j.Logger;
import org.lwjgl.Sys;
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
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiGameOver;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSleepMP;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.ScreenChatOptions;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.chunk.RenderChunk;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.profiler.Profiler;
import net.minecraft.profiler.Snooper;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.FrameTimer;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ScreenShotHelper;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.common.MinecraftForge;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	Minecraft mc= Minecraft.getMinecraft();
	
	GuiScreen customCurrentScreen=null;
	boolean guiflag=false;
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
	private static int debugFPS;
	@Shadow
	private String debug;
	@Shadow
	private Snooper usageSnooper;
	@Shadow
	private IntegratedServer integratedServer;
	@Shadow	
	private WorldClient world;
	@Shadow
	private RayTraceResult objectMouseOver;
	@Shadow
	private TextureManager renderEngine;
	@Shadow
	private int leftClickCounter;
	@Shadow
	private int joinPlayerCounter;
	@Shadow
	private MusicTicker mcMusicTicker;
	@Shadow
	private ParticleManager effectRenderer;
	@Shadow
	private NetworkManager myNetworkManager;
	@Shadow
	private long systemTime;
	@Shadow
	private File mcDataDir;
    @Shadow
    public abstract void sendClickBlockToController(boolean leftClick);
    @Shadow
    private static Logger LOGGER;
    
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
                Util.runTask(this.scheduledTasks.poll(), LOGGER);
            }
        }

        this.mcProfiler.endSection();
        long l = System.nanoTime();
        this.mcProfiler.startSection("tick");
        if(TickrateChanger.TICKS_PER_SECOND==0) {
        	redoRunGameLoopHead();
        }else {
	        for (int j = 0; j < Math.min(10, this.timer.elapsedTicks); ++j)
	        {
	            this.redoRunTick();
	        }
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

        while (Minecraft.getSystemTime() >= this.debugUpdateTime + 1000L)
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
    
	private void redoRunGameLoopHead() throws IOException {
		TickrateChanger.TASTIMER.updateTimer();
		for (int j = 0; j < Math.min(10, TickrateChanger.TASTIMER.elapsedTicks); ++j)
        {
            MinecraftForge.EVENT_BUS.post(new TASTickEvent());
        }
		if(TickrateChanger.TICKS_PER_SECOND==0) {
			testBypass();
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
		if(TickrateChanger.WASZERO&&TickrateChanger.TICKS_PER_SECOND!=0&&currentScreen==null) {
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
	
	private void redoRunTick() throws IOException {
		if (this.rightClickDelayTimer > 0)
        {
            --this.rightClickDelayTimer;
        }

        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPreClientTick();

        this.mcProfiler.startSection("gui");

        if (!this.isGamePaused)
        {
            this.ingameGUI.updateTick();
        }

        this.mcProfiler.endSection();
        this.entityRenderer.getMouseOver(1.0F);
        this.tutorial.onMouseHover(this.world, this.objectMouseOver);
        this.mcProfiler.startSection("gameMode");

        if (!this.isGamePaused && this.world != null)
        {
            this.playerController.updateController();
        }

        this.mcProfiler.endStartSection("textures");

        if (this.world != null)
        {
            this.renderEngine.tick();
        }
        if (this.currentScreen == null && this.player != null)
        {
            if (this.player.getHealth() <= 0.0F && !(this.currentScreen instanceof GuiGameOver))
            {
                this.displayGuiScreen((GuiScreen)null);
            }
            else if (this.player.isPlayerSleeping() && this.world != null)
            {
                this.displayGuiScreen(new GuiSleepMP());
            }
        }
        else if (this.currentScreen != null && this.currentScreen instanceof GuiSleepMP && !this.player.isPlayerSleeping())
        {
            this.displayGuiScreen((GuiScreen)null);
        }

        if (this.currentScreen != null)
        {
            this.leftClickCounter = 10000;
        }
        if (this.currentScreen != null&&TickrateChanger.ADVANCE_TICK==false)
        {
            try
            {
                this.currentScreen.handleInput();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addDetail("Screen name", new ICrashReportDetail<String>()
                {
                    public String call() throws Exception
                    {
                        return ((Minecraft)(Object)this).currentScreen.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }

            if (this.currentScreen != null)
            {
                try
                {
                    this.currentScreen.updateScreen();
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
                    crashreportcategory1.addDetail("Screen name", new ICrashReportDetail<String>()
                    {
                        public String call() throws Exception
                        {
                            return ((Minecraft)(Object)this).currentScreen.getClass().getCanonicalName();
                        }
                    });
                    throw new ReportedException(crashreport1);
                }
            }
        }
        if (this.currentScreen == null || this.currentScreen.allowUserInput)
        {
        	this.mcProfiler.endStartSection("mouse");
        	this.runTickMouse();
	
	        if (this.leftClickCounter > 0)
	        {
	        	--this.leftClickCounter;
	        }
	        
	        this.mcProfiler.endStartSection("keyboard");
	        redoRunTickKeyboard();
	    }
        if (this.world != null)
        {
            if (this.player != null)
            {
                ++this.joinPlayerCounter;

                if (this.joinPlayerCounter == 30)
                {
                    this.joinPlayerCounter = 0;
                    this.world.joinEntityInSurroundings(this.player);
                }
            }

            this.mcProfiler.endStartSection("gameRenderer");

            if (!this.isGamePaused)
            {
                this.entityRenderer.updateRenderer();
            }

            this.mcProfiler.endStartSection("levelRenderer");

            if (!this.isGamePaused)
            {
                this.renderGlobal.updateClouds();
            }

            this.mcProfiler.endStartSection("level");

            if (!this.isGamePaused)
            {
                if (this.world.getLastLightningBolt() > 0)
                {
                    this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
                }

                this.world.updateEntities();
            }
        }
        else if (this.entityRenderer.isShaderActive())
        {
            this.entityRenderer.stopUseShader();
        }

        if (!this.isGamePaused)
        {
            this.mcMusicTicker.update();
            this.mcSoundHandler.update();
        }

        if (this.world != null)
        {
            if (!this.isGamePaused)
            {
                this.world.setAllowedSpawnTypes(this.world.getDifficulty() != EnumDifficulty.PEACEFUL, true);
                this.tutorial.update();

                try
                {
                    this.world.tick();
                }
                catch (Throwable throwable2)
                {
                    CrashReport crashreport2 = CrashReport.makeCrashReport(throwable2, "Exception in world tick");

                    if (this.world == null)
                    {
                        CrashReportCategory crashreportcategory2 = crashreport2.makeCategory("Affected level");
                        crashreportcategory2.addCrashSection("Problem", "Level is null!");
                    }
                    else
                    {
                        this.world.addWorldInfoToCrashReport(crashreport2);
                    }

                    throw new ReportedException(crashreport2);
                }
            }

            this.mcProfiler.endStartSection("animateTick");

            if (!this.isGamePaused && this.world != null)
            {
                this.world.doVoidFogParticles(MathHelper.floor(this.player.posX), MathHelper.floor(this.player.posY), MathHelper.floor(this.player.posZ));
            }

            this.mcProfiler.endStartSection("particles");

            if (!this.isGamePaused)
            {
                this.effectRenderer.updateEffects();
            }
        }
        else if (this.myNetworkManager != null)
        {
            this.mcProfiler.endStartSection("pendingConnection");
            this.myNetworkManager.processReceivedPackets();
        }

        this.mcProfiler.endSection();
        net.minecraftforge.fml.common.FMLCommonHandler.instance().onPostClientTick();
        this.systemTime = this.getSystemTimer();
	}
    private long getSystemTimer() {
    	return Sys.getTime() * 1000L / Sys.getTimerResolution();
    }
	@Shadow
	protected abstract void runTickKeyboard();
	@Shadow
	protected abstract void runTickMouse();
	
	public void redoRunTickKeyboard() throws IOException {
    	while (Keyboard.next())
        {
            int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

            if (this.debugCrashKeyPressTime > 0L)
            {
                if (Minecraft.getSystemTime() - this.debugCrashKeyPressTime >= 6000L)
                {
                    throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
                }

                if (!Keyboard.isKeyDown(46) || !Keyboard.isKeyDown(61))
                {
                    this.debugCrashKeyPressTime = -1L;
                }
            }
            else if (Keyboard.isKeyDown(46) && Keyboard.isKeyDown(61))
            {
                this.actionKeyF3 = true;
                this.debugCrashKeyPressTime = Minecraft.getSystemTime();
            }

            this.dispatchKeypresses();

            if (this.currentScreen != null)
            {
                this.currentScreen.handleKeyboardInput();
            }

            boolean flag = Keyboard.getEventKeyState();

            if (flag)
            {
                if (i == 62 && this.entityRenderer != null)
                {
                    this.entityRenderer.switchUseShader();
                }

                boolean flag1 = false;

                if (this.currentScreen == null)
                {
                    if (i == 1)
                    {
                        this.displayInGameMenu();
                    }

                    flag1 = Keyboard.isKeyDown(61) && this.processKeyF3(i);
                    this.actionKeyF3 |= flag1;

                    if (i == 59)
                    {
                        this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
                    }
                }

                if (flag1)
                {
                    KeyBinding.setKeyBindState(i, false);
                }
                else
                {
                    KeyBinding.setKeyBindState(i, true);
                    KeyBinding.onTick(i);
                }

                if (this.gameSettings.showDebugProfilerChart)
                {
                    if (i == 11)
                    {
                        this.updateDebugProfilerName(0);
                    }

                    for (int j = 0; j < 9; ++j)
                    {
                        if (i == 2 + j)
                        {
                            this.updateDebugProfilerName(j + 1);
                        }
                    }
                }
            }
            else
            {
                KeyBinding.setKeyBindState(i, false);

                if (i == 61)
                {
                    if (this.actionKeyF3)
                    {
                        this.actionKeyF3 = false;
                    }
                    else
                    {
                        this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
                        this.gameSettings.showDebugProfilerChart = this.gameSettings.showDebugInfo && GuiScreen.isShiftKeyDown();
                        this.gameSettings.showLagometer = this.gameSettings.showDebugInfo && GuiScreen.isAltKeyDown();
                    }
                }
            }
            if(!(TickrateChanger.TICKS_PER_SECOND==0&&currentScreen instanceof GuiChat)) {
            	net.minecraftforge.fml.common.FMLCommonHandler.instance().fireKeyInput();		//Don't execute mod keybinds when tickrate == 0 and the Chat is open. Prevents executing stuff, when typing in chat
            }
        }

        this.redoProcessKeyBinds();
    }
    
    private void redoProcessKeyBinds()
    {
    	if (TickrateContainer.KEY_ADVANCE.isPressed()) {
        	TickrateAPI.advanceTick();
        }
        if(TickrateContainer.KEY_PAUSE.isPressed()) {
        	TickrateAPI.pauseUnpauseGame();
        }
    	if(TickrateChanger.TICKS_PER_SECOND!=0) {
    		if(TickrateChanger.ADVANCE_TICK==false) {
    			/*+++++++++++++++++++++++++++++ VANILLA SECTION +++++++++++++++++++++++++++++*/
		        for (; this.gameSettings.keyBindTogglePerspective.isPressed(); this.renderGlobal.setDisplayListEntitiesDirty())
		        {
		            ++this.gameSettings.thirdPersonView;
		
		            if (this.gameSettings.thirdPersonView > 2)
		            {
		                this.gameSettings.thirdPersonView = 0;
		            }
		
		            if (this.gameSettings.thirdPersonView == 0)
		            {
		                this.entityRenderer.loadEntityShader(this.getRenderViewEntity());
		            }
		            else if (this.gameSettings.thirdPersonView == 1)
		            {
		                this.entityRenderer.loadEntityShader((Entity)null);
		            }
		        }
		        
		        while (this.gameSettings.keyBindSmoothCamera.isPressed())
		        {
		            this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
		        }
		
		        for (int i = 0; i < 9; ++i)
		        {
		            boolean flag = this.gameSettings.keyBindSaveToolbar.isKeyDown();
		            boolean flag1 = this.gameSettings.keyBindLoadToolbar.isKeyDown();
		        
		            if (this.gameSettings.keyBindsHotbar[i].isPressed())
		            {
		                if (this.player.isSpectator())
		                {
		                    this.ingameGUI.getSpectatorGui().onHotbarSelected(i);
		                }
		                else if (!this.player.isCreative() || this.currentScreen != null || !flag1 && !flag)
		                {
		                    this.player.inventory.currentItem = i;
		                }
		                else
		                {
		                    GuiContainerCreative.handleHotbarSnapshots(Minecraft.getMinecraft(), i, flag1, flag);
		                }
		            }
		        }
		        while (this.gameSettings.keyBindInventory.isPressed())
		        {
		            if (this.playerController.isRidingHorse())
		            {
		                this.player.sendHorseInventory();
		            }
		            else
		            {
		                this.tutorial.openInventory();
		                this.displayGuiScreen(new GuiInventory(this.player));
		            }
		        }
		        while (this.gameSettings.keyBindAdvancements.isPressed())
		        {
		            this.displayGuiScreen(new GuiScreenAdvancements(this.player.connection.getAdvancementManager()));
		        }
		
		        while (this.gameSettings.keyBindSwapHands.isPressed())
		        {
		            if (!this.player.isSpectator())
		            {
		                this.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
		            }
		        }
		
		        while (this.gameSettings.keyBindDrop.isPressed())
		        {
		            if (!this.player.isSpectator())
		            {
		                this.player.dropItem(GuiScreen.isCtrlKeyDown());
		            }
		        }
		        boolean flag2 = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;
		        if (flag2)
		        {
		            while (this.gameSettings.keyBindChat.isPressed())
		            {
		                this.displayGuiScreen(new GuiChat());
		            }
		
		            if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed())
		            {
		                this.displayGuiScreen(new GuiChat("/"));
		            }
		        }
    		}
    		/*====================================== END OF VANILLA SECTION ======================================*/
    		/*++++++++++++ Changes Tickrate to 0 after the Ingame Menu sets it to 20 +++++++++++++++*/
			if(!(currentScreen instanceof GuiIngameMenu)&&TickrateChanger.WASZERO==true) {
				TickrateAPI.changeClientTickrate(0,false);
			}
			/*=====================================================================================*/
		} else {
			/*+++++++++++++++++Fixing issue: After sending a chat message, keybinds get executed +++++++++++++++++*/
			if (!(this.currentScreen instanceof GuiChat)) {
				boolean flag2 = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;
				if (flag2) {
					while (this.gameSettings.keyBindChat.isPressed()) {
						this.displayGuiScreen(new GuiChat());
					}

					if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed()) {
						this.displayGuiScreen(new GuiChat("/"));
					}
				}
			}else {
				for(KeyBinding x : this.gameSettings.keyBindings) {
					x.pressTime=0;
				}
			}
			/*========================================================================================================*/
			/*++++++++++++++++++++++++++++++ When the Tickadvance Key is pressed +++++++++++++++++++++++++++++++
			 * 						(Fixes an issue, so the inventory isn't open in tickrate 0)					*/
			if(TickrateChanger.ADVANCE_TICK==true) {
				for (; this.gameSettings.keyBindTogglePerspective.isPressed(); this.renderGlobal.setDisplayListEntitiesDirty())
		        {
		            ++this.gameSettings.thirdPersonView;
		
		            if (this.gameSettings.thirdPersonView > 2)
		            {
		                this.gameSettings.thirdPersonView = 0;
		            }
		
		            if (this.gameSettings.thirdPersonView == 0)
		            {
		                this.entityRenderer.loadEntityShader(this.getRenderViewEntity());
		            }
		            else if (this.gameSettings.thirdPersonView == 1)
		            {
		                this.entityRenderer.loadEntityShader((Entity)null);
		            }
		        }
		        
		        while (this.gameSettings.keyBindSmoothCamera.isPressed())
		        {
		            this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
		        }
		        
		        for (int i = 0; i < 9; ++i)
		        {
		            boolean flag = this.gameSettings.keyBindSaveToolbar.isKeyDown();
		            boolean flag1 = this.gameSettings.keyBindLoadToolbar.isKeyDown();
		        
		            if (this.gameSettings.keyBindsHotbar[i].isPressed())
		            {
		                if (this.player.isSpectator())
		                {
		                    this.ingameGUI.getSpectatorGui().onHotbarSelected(i);
		                }
		                else if (!this.player.isCreative() || this.currentScreen != null || !flag1 && !flag)
		                {
		                    this.player.inventory.currentItem = i;
		                }
		                else
		                {
		                    GuiContainerCreative.handleHotbarSnapshots(Minecraft.getMinecraft(), i, flag1, flag);
		                }
		            }
		        }
		        if (this.gameSettings.keyBindInventory.isPressed())
		        {
		        	if(currentScreen == null) {
		            if (this.playerController.isRidingHorse())
		            {
		                this.player.sendHorseInventory();
		            }
		            else
		            {
		                this.tutorial.openInventory();
		                this.displayGuiScreen(new GuiInventory(this.player));
		            }
		        	}else {
		        		this.gameSettings.keyBindInventory.pressTime=0;
		        		displayGuiScreen(null);
		        	}
		        }
		        while (this.gameSettings.keyBindAdvancements.isPressed())
		        {
		            this.displayGuiScreen(new GuiScreenAdvancements(this.player.connection.getAdvancementManager()));
		        }
		
		        while (this.gameSettings.keyBindSwapHands.isPressed())
		        {
		            if (!this.player.isSpectator())
		            {
		                this.getConnection().sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, BlockPos.ORIGIN, EnumFacing.DOWN));
		            }
		        }
		
		        while (this.gameSettings.keyBindDrop.isPressed())
		        {
		            if (!this.player.isSpectator())
		            {
		                this.player.dropItem(GuiScreen.isCtrlKeyDown());
		            }
		        }
		        boolean flag2 = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;
		        if (flag2)
		        {
		            while (this.gameSettings.keyBindChat.isPressed())
		            {
		                this.displayGuiScreen(new GuiChat());
		            }
		
		            if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed())
		            {
		                this.displayGuiScreen(new GuiChat("/"));
		            }
		        }
			}
			/*Changes client Tickrate to 20 when the IngameMenu is open, to be able to close the game*/
			if(currentScreen instanceof GuiIngameMenu) {
				TickrateAPI.changeClientTickrate(20,false);
			}
		}
        if (this.player.isHandActive())
        {
            if (!this.gameSettings.keyBindUseItem.isKeyDown())
            {
                this.playerController.onStoppedUsingItem(this.player);
            }

            label109:

            while (true)
            {
                if (!this.gameSettings.keyBindAttack.isPressed())
                {
                    while (this.gameSettings.keyBindUseItem.isPressed())
                    {
                        ;
                    }

                    while (true)
                    {
                        if (this.gameSettings.keyBindPickBlock.isPressed())
                        {
                            continue;
                        }

                        break label109;
                    }
                }
            }
        }
        else
        {
            while (this.gameSettings.keyBindAttack.isPressed())
            {
                this.clickMouse();
            }

            while (this.gameSettings.keyBindUseItem.isPressed())
            {
                this.rightClickMouse();
            }

            while (this.gameSettings.keyBindPickBlock.isPressed())
            {
                this.middleClickMouse();
            }
        }
        if (this.gameSettings.keyBindUseItem.isKeyDown() && this.rightClickDelayTimer == 0 && !this.player.isHandActive())
        {
            this.rightClickMouse();
        }

        this.sendClickBlockToController(this.currentScreen == null && this.gameSettings.keyBindAttack.isKeyDown() && this.inGameHasFocus);
    }
    
    
    @Shadow
	protected abstract int getLimitFramerate();
    @Shadow
	protected abstract boolean isFramerateLimitBelowMax();
    @Shadow
	protected abstract boolean isSingleplayer();
    @Shadow
	protected abstract void updateDisplay();
    @Shadow
	protected abstract void displayDebugInfo(long i1);
    @Shadow
	protected abstract void runTick();
    @Shadow
	protected abstract void checkGLError(String string);
    @Shadow
	protected abstract void shutdown();
	@Shadow
	public abstract void updateDebugProfilerName(int i);
    @Shadow
    public abstract boolean processKeyF3(int i);
    @Shadow
    public abstract void displayInGameMenu();
    @Shadow
    public abstract void dispatchKeypresses();
    
    private void testBypass() throws IOException {
    	if (this.currentScreen != null&&TickrateChanger.ADVANCE_TICK==true)
        {
            try
            {
                this.currentScreen.handleInput();
            }
            catch (Throwable throwable1)
            {
                CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Updating screen events");
                CrashReportCategory crashreportcategory = crashreport.makeCategory("Affected screen");
                crashreportcategory.addDetail("Screen name", new ICrashReportDetail<String>()
                {
					public String call() throws Exception
                    {
                        return ((Minecraft)(Object)this).currentScreen.getClass().getCanonicalName();
                    }
                });
                throw new ReportedException(crashreport);
            }
            if (this.currentScreen != null)
            {
                try
                {
                    this.currentScreen.updateScreen();
                }
                catch (Throwable throwable)
                {
                    CrashReport crashreport1 = CrashReport.makeCrashReport(throwable, "Ticking screen");
                    CrashReportCategory crashreportcategory1 = crashreport1.makeCategory("Affected screen");
                    crashreportcategory1.addDetail("Screen name", new ICrashReportDetail<String>()
                    {
                        public String call() throws Exception
                        {
                            return ((Minecraft)(Object)this).currentScreen.getClass().getCanonicalName();
                        }
                    });
                    throw new ReportedException(crashreport1);
                }
            }
        }
        redoRunTickKeyboard();
    }
    @Inject(method="dispatchKeypresses", at= @At("HEAD"), cancellable=true)
    public void redoDispatchKeypresses(CallbackInfo ci) {
    	int i = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();

        if (i != 0 && !Keyboard.isRepeatEvent())
        {
            if (!(this.currentScreen instanceof GuiControls) || ((GuiControls)this.currentScreen).time <= Minecraft.getSystemTime() - 20L)
            {
                if (Keyboard.getEventKeyState())
                {
                    if (this.gameSettings.keyBindFullscreen.isActiveAndMatches(i))
                    {
                        this.toggleFullscreen();
                    }
                    else if (this.gameSettings.keyBindScreenshot.isActiveAndMatches(i))
                    {
                        this.ingameGUI.getChatGUI().printChatMessage(ScreenShotHelper.saveScreenshot(this.mcDataDir, this.displayWidth, this.displayHeight, this.framebufferMc));
                    }
                    else if (i == 48 && GuiScreen.isCtrlKeyDown() && (this.currentScreen == null || this.currentScreen != null && !this.currentScreen.isFocused()))
                    {
                        this.gameSettings.setOptionValue(GameSettings.Options.NARRATOR, 1);

                        if (this.currentScreen instanceof ScreenChatOptions)
                        {
                            ((ScreenChatOptions)this.currentScreen).updateNarratorButton();
                        }
                    }
                }
                else if (this.currentScreen instanceof GuiControls) ((GuiControls)this.currentScreen).buttonId = null;
            }
        }
    }
    @Shadow
	protected abstract void toggleFullscreen();
}
