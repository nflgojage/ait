package loqor.ait.client.registry.console.impl;

import loqor.ait.AITMod;
import loqor.ait.client.models.consoles.ConsoleModel;
import loqor.ait.client.models.consoles.HartnellConsoleModel;
import loqor.ait.client.registry.console.ClientConsoleVariantSchema;
import loqor.ait.tardis.console.variant.hartnell.KeltHartnellVariant;
import net.minecraft.util.Identifier;

public class ClientKeltHartnellVariant extends ClientConsoleVariantSchema {
	public static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID, ("textures/blockentities/consoles/hartnell_kelt_console.png"));
	public static final Identifier EMISSION = new Identifier(AITMod.MOD_ID, ("textures/blockentities/consoles/hartnell_console_emission.png"));

	public ClientKeltHartnellVariant() {
		super(KeltHartnellVariant.REFERENCE, KeltHartnellVariant.REFERENCE);
	}

	@Override
	public Identifier texture() {
		return TEXTURE;
	}

	@Override
	public Identifier emission() {
		return EMISSION;
	}

	@Override
	public ConsoleModel model() {
		return new HartnellConsoleModel(HartnellConsoleModel.getTexturedModelData().createModel());
	}
}
