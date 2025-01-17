package loqor.ait.client.registry.console.impl;

import loqor.ait.AITMod;
import loqor.ait.client.models.consoles.ConsoleModel;
import loqor.ait.client.models.consoles.ToyotaConsoleModel;
import loqor.ait.client.registry.console.ClientConsoleVariantSchema;
import loqor.ait.tardis.console.variant.toyota.ToyotaBlueVariant;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

public class ClientToyotaBlueVariant extends ClientConsoleVariantSchema {
	public static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID, ("textures/blockentities/consoles/toyota_default.png"));
	public static final Identifier EMISSION = new Identifier(AITMod.MOD_ID, ("textures/blockentities/consoles/toyota_blue_emission.png"));

	public ClientToyotaBlueVariant() {
		super(ToyotaBlueVariant.REFERENCE, ToyotaBlueVariant.REFERENCE);
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
		return new ToyotaConsoleModel(ToyotaConsoleModel.getTexturedModelData().createModel());
	}

	@Override
	public Vector3f sonicItemTranslations() {
		return new Vector3f(-0.5275f, 1.35f, 0.7f);
	}

	@Override
	public float[] sonicItemRotations() {
		return new float[]{-120f, -45f};
	}
}
