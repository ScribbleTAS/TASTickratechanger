package me.guichaguri.tastickratechanger.ticksync;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class TickSyncServer {
	private static int serverticksync=0;
	private static boolean enabled=false;
	
	public static void sync(boolean enable) {
		enabled=enable;
	}
	public static void incrementServerTickCounter() {
		serverticksync++;
	}
	public static void resetTickCounter(){
		serverticksync=0;
	}
	public static int getServertickcounter() {
		return serverticksync;
	}
	public static boolean isEnabled() {
		return enabled;
	}
}
