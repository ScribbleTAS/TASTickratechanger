package me.guichaguri.tickratechanger;

import java.util.Queue;
import java.util.concurrent.FutureTask;

import com.ibm.icu.impl.ICUService.Key;

import me.guichaguri.tickratechanger.api.TickrateAPI;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;

public class TickEvents {
	private static int tickspassed=0;
	@SubscribeEvent
	public void onTick(TickEvent.RenderTickEvent ev) {
		if(ev.phase==Phase.START) {
			if(TickrateChanger.MILISECONDS_PER_TICK==Long.MAX_VALUE&&tickspassed==0) {
				FMLCommonHandler.instance().fireKeyInput();
			}
			tickspassed++;
			if(tickspassed==20) {
				tickspassed=0;
			}
		}
	}
}
