package loqor.ait.client.registry.exterior.impl.easter_head;

import loqor.ait.AITMod;
import loqor.ait.client.models.exteriors.EasterHeadModel;
import loqor.ait.client.models.exteriors.ExteriorModel;
import loqor.ait.client.registry.exterior.ClientExteriorVariantSchema;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;

// a useful class for creating easter_head variants as they all have the same filepath you know
public abstract class ClientEasterHeadVariant extends ClientExteriorVariantSchema {
	private final String name;
	protected static final String TEXTURE_PATH = "textures/blockentities/exteriors/easter_head/easter_head_";

	protected ClientEasterHeadVariant(String name) {
		super(new Identifier(AITMod.MOD_ID, "exterior/easter_head/" + name));

		this.name = name;
	}


	@Override
	public ExteriorModel model() {
		return new EasterHeadModel(EasterHeadModel.getTexturedModelData().createModel());
	}

	@Override
	public Identifier texture() {
		return new Identifier(AITMod.MOD_ID, TEXTURE_PATH + name + ".png");
	}

	@Override
	public Identifier emission() {
		return null;
	}

	@Override
	public Vector3f sonicItemTranslations() {
		return new Vector3f(0.25f, 1.1f, 1.2f);
	}

	@Override
	public float[] sonicItemRotations() {
		return new float[]{0f, 112.5f};
	}
}