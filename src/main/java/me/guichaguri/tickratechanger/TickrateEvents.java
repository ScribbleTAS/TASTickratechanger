package me.guichaguri.tickratechanger;

import java.util.Queue;
import java.util.concurrent.FutureTask;

import com.ibm.icu.impl.ICUService.Key;

import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class TickrateEvents {
private int tickcount=0;

	
	@SubscribeEvent
	public void onTick(TickEvent.ClientTickEvent ev) {
		if (ev.phase == Phase.START) {
			if (FMLCommonHandler.instance().getMinecraftServerInstance()!=null&&TickrateAPI.getClientTickrate() <= TickrateChanger.TICKCOUNTERBOARDER) {
				if(Minecraft.getMinecraft().currentScreen instanceof GuiIngameMenu) {
					return;
				}
				tickcount++;
				TickrateChanger.LOGGER.info("Tickcount: " + tickcount);
			}else if((FMLCommonHandler.instance().getMinecraftServerInstance()==null||TickrateAPI.getClientTickrate()>TickrateChanger.TICKCOUNTERBOARDER)&&tickcount!=0) {
				tickcount=0;
			}
		}
	}
}
