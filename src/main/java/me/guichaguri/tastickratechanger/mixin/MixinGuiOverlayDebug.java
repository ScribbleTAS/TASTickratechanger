package me.guichaguri.tastickratechanger.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import me.guichaguri.tastickratechanger.ticksync.TickSync;
import net.minecraft.client.gui.GuiOverlayDebug;

@Mixin(GuiOverlayDebug.class)
public class MixinGuiOverlayDebug {
	@ModifyVariable(method="getDebugInfoRight", at=@At(value = "STORE", ordinal = 0))
	public List<String> addTickInfo(List<String> list) {
		list.add("");
		list.add("Client: "+TickSync.getClienttickcounter());
		list.add("Server: "+TickSync.getServertickcounter());
		return list;
	}
}
