package me.guichaguri.tastickratechanger;

import me.guichaguri.tastickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class TickrateEvents {
private int tickcount=0;


	
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent ev) {
		if (ev.phase == Phase.START) {
			if (FMLCommonHandler.instance().getMinecraftServerInstance()!=null&&TickrateAPI.getClientTickrate() <= TickrateChanger.TICKCOUNTERBOARDER) {
				if(!(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu)) {
					tickcount++;
					TickrateChanger.LOGGER.info("Tickcount: " + tickcount);
				}
			}else if((FMLCommonHandler.instance().getMinecraftServerInstance()==null||TickrateAPI.getClientTickrate()>TickrateChanger.TICKCOUNTERBOARDER)&&tickcount!=0) {
				tickcount=0;
			}
			/*if (TickrateContainer.KEY_CLOSEGUIS.isPressed()) {
				if (TickrateAPI.getClientTickrate() != 0) {
					Minecraft.getMinecraft().displayGuiScreen(null);
				}
			}
			if(TickrateChanger.ADVANCE_TICK) {
				TickrateAPI.handlePausingGame(true);
				TickrateChanger.ADVANCE_TICK=false;
			}*/
		}
	}
	@SubscribeEvent
	public void onMenu(GuiOpenEvent ev) {
		if(ev.getGui() instanceof GuiMainMenu) {
			TickrateChanger.WASZERO=false;
		}
	}
}
