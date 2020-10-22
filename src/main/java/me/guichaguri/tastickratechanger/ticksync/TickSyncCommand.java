package me.guichaguri.tastickratechanger.ticksync;

import java.util.List;

import me.guichaguri.tastickratechanger.TickrateChanger;
import me.guichaguri.tastickratechanger.TickrateContainer;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;

public class TickSyncCommand extends CommandBase{

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "ticksync";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		// TODO Auto-generated method stub
		return "/ticksync [reset]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length==0) {
			TickSyncServer.sync(!TickSyncServer.isEnabled());
			TickSyncServer.resetTickCounter();
			TickrateChanger.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(),true,!TickSyncServer.isEnabled()));
		}else if(args[0].equalsIgnoreCase("reset")&&args.length==1) {
			TickSyncServer.resetTickCounter();
			TickrateChanger.NETWORK.sendToAll(new TickSyncPackage(TickSyncServer.getServertickcounter(),true,TickSyncServer.isEnabled()));
		}
	}
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		return getListOfStringsMatchingLastWord(args, "reset");
	}

}
