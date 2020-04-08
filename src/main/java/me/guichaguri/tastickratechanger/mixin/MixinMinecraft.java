package me.guichaguri.tastickratechanger.mixin;

import java.io.IOException;

import javax.annotation.Nullable;

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import me.guichaguri.tastickratechanger.TASTickEvent;
import me.guichaguri.tastickratechanger.TickrateChanger;
import me.guichaguri.tastickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.advancements.GuiScreenAdvancements;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.tutorial.Tutorial;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.crash.ICrashReportDetail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft {
	Minecraft mc= Minecraft.getMinecraft();
	
	GuiScreen customCurrentScreen=null;
	boolean guiflag=false;
	@Shadow 
	public abstract void runTickKeyboard();
	@Shadow
	private SoundHandler mcSoundHandler;
	
	@Inject(method = "runGameLoop", at = @At(value="HEAD"))
	private void redoRunGameLoopHead(CallbackInfo ci) {
		TickrateChanger.TASTIMER.updateTimer();
		for (int j = 0; j < Math.min(10, TickrateChanger.TASTIMER.elapsedTicks); ++j)
        {
            MinecraftForge.EVENT_BUS.post(new TASTickEvent());
        }
		if(TickrateChanger.TICKS_PER_SECOND==0) {
			TickrateChanger.WASZERO=true;
			//this.mcSoundHandler.pauseSounds();
			runTickKeyboard();
		}
	}
	@Inject(method = "runGameLoop", at = @At(value="RETURN"))
	private void redoRunGameLoopReturn(CallbackInfo ci) {
		if(TickrateChanger.WASZERO&&TickrateChanger.TICKS_PER_SECOND!=0) {
			this.mcSoundHandler.resumeSounds();
			System.out.println("Heck");
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
	private boolean actionKeyF3;
    @Shadow
    public abstract void sendClickBlockToController(boolean leftClick);
    
    private void processKeyBinds()
    {
    	if(TickrateChanger.TICKS_PER_SECOND!=0) {
    		if(TickrateChanger.ADVANCE_TICK==false) {
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
			if(!(currentScreen instanceof GuiIngameMenu)&&TickrateChanger.WASZERO==true) {
				TickrateAPI.changeClientTickrate(0,false);
			}
		} else {
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
    
    @Inject(method="runTickKeyboard", at=@At("HEAD"), cancellable=true)
    public void redorunTickKeyboard(CallbackInfo ci) throws IOException {
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
            	net.minecraftforge.fml.common.FMLCommonHandler.instance().fireKeyInput();
            }
        }

        this.processKeyBinds();
        ci.cancel();
    }
    
	@Shadow
	public abstract void updateDebugProfilerName(int i);
    @Shadow
    public abstract boolean processKeyF3(int i);
    @Shadow
    public abstract void displayInGameMenu();
    @Shadow
    public abstract void dispatchKeypresses();
}
