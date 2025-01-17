package loqor.ait.tardis.control.impl;

import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.control.Control;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class LandTypeControl extends Control {
	public LandTypeControl() {
		super("land_type");
	}

	@Override
	public boolean runServer(Tardis tardis, ServerPlayerEntity player, ServerWorld world) {

		if (tardis.getHandlers().getSequenceHandler().hasActiveSequence()) {
			if (tardis.getHandlers().getSequenceHandler().controlPartOfSequence(this)) {
				this.addToControlSequence(tardis);
				return false;
			}
		}

		PropertiesHandler.set(tardis, PropertiesHandler.FIND_GROUND, !PropertiesHandler.getBool(tardis.getHandlers().getProperties(), PropertiesHandler.FIND_GROUND));
		messagePlayer(player, PropertiesHandler.getBool(tardis.getHandlers().getProperties(), PropertiesHandler.FIND_GROUND));

		return false;
	}

	public void messagePlayer(ServerPlayerEntity player, boolean var) {
		String s = var ? "ON" : "OFF";
		Text on = Text.translatable("tardis.message.control.landtype.on");
		Text off = Text.translatable("tardis.message.control.landtype.off");
		player.sendMessage((var ? on : off), true);
	}
}
