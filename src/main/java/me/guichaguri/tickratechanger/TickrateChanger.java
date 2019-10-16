package me.guichaguri.tickratechanger;

import java.io.File;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.IFMLCallHook;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.TransformerExclusions;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import me.guichaguri.tickratechanger.mixin.MixinMinecraft;
import me.guichaguri.tickratechanger.mixin.MixinMinecraftServer;

/**
 * @author Guilherme Chaguri
 */
public class TickrateChanger {

    public static TickrateChanger INSTANCE;
    public static Logger LOGGER = LogManager.getLogger("Tickrate Changer");
    public static SimpleNetworkWrapper NETWORK;
    public static TickrateCommand COMMAND = null;
    public static File CONFIG_FILE = null;

    public static final String MODID = "tickratechanger";
    public static final String VERSION = "1.0.13";

    public static final String GAME_RULE = "tickrate";

    // Default tickrate - can be changed in the config file
    public static float DEFAULT_TICKRATE = 20;
    // Stored client-side tickrate
    public static float TICKS_PER_SECOND = 20;
    // Server-side tickrate in miliseconds
    public static long MILISECONDS_PER_TICK = 50L;
    // Sound speed
    public static float GAME_SPEED = 1;
    // Min Tickrate
    public static float MIN_TICKRATE = 0.1F;
    // Max Tickrate
    public static float MAX_TICKRATE = 1000;
    // Show Messages
    public static boolean SHOW_MESSAGES = true;
    // Change sound speed
    public static boolean CHANGE_SOUND = true;
    // Interrupt Server Sleep
    public static boolean INTERRUPT;

    public TickrateChanger() {
        INSTANCE = this;
    }


    @SideOnly(Side.CLIENT)
    public void updateClientTickrate(float tickrate, boolean log) {
        if(log) LOGGER.info("Updating client tickrate to " + tickrate);

        TICKS_PER_SECOND = tickrate;
        if(CHANGE_SOUND) GAME_SPEED = tickrate / 20F;

        Minecraft mc = Minecraft.getMinecraft();
        if(mc == null) return; // Wut
        if(tickrate>0) {
        	mc.timer.tickLength = 1000F / tickrate;
        }else if(tickrate==0) {
        	mc.timer.tickLength=Float.MAX_VALUE;
        }
    }

    public void updateServerTickrate(float tickrate, boolean log) {
    	INTERRUPT=true;
        if(log) LOGGER.info("Updating server tickrate to " + tickrate);
        if(tickrate>0) {
        	MILISECONDS_PER_TICK = (long)(1000L / tickrate);
        }else if(tickrate==0) {
        	MILISECONDS_PER_TICK = Long.MAX_VALUE;
        }
    }
}
