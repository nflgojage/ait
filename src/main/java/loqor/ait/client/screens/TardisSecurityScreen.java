package loqor.ait.client.screens;

import com.google.common.collect.Lists;
import loqor.ait.AITMod;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.PressableTextWidget;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.*;
import java.util.List;
import java.util.UUID;

public class TardisSecurityScreen extends ConsoleScreen {
	private static final Identifier TEXTURE = new Identifier(AITMod.MOD_ID, "textures/gui/tardis/consoles/monitors/security_menu.png");
	private final List<ButtonWidget> buttons = Lists.newArrayList();
	int bgHeight = 117;
	int bgWidth = 191;
	int left, top;
	private final int tickForSpin = 0;
	int choicesCount = 0;
	private final Screen parent;

	public TardisSecurityScreen(UUID tardis, UUID console, Screen parent) {
		super(Text.translatable("screen.ait.security.title"), tardis, console);
		this.parent = parent;
		updateTardis();
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	protected void init() {
		this.top = (this.height - this.bgHeight) / 2; // this means everythings centered and scaling, same for below
		this.left = (this.width - this.bgWidth) / 2;
		this.createButtons();

		super.init();
	}

	private void createButtons() {
		choicesCount = 0;
		this.buttons.clear();

		createTextButton(Text.translatable("screen.ait.interiorsettings.back"), (button -> backToExteriorChangeScreen()));
		createTextButton(Text.translatable("screen.ait.security.leave_behind"), (button -> toggleLeaveBehind()));
		createTextButton(Text.translatable("screen.ait.security.hostile_alarms"), (button -> toggleHostileAlarms()));
		/*createTextButton(Text.literal("> Shields"), (button -> toggleShields()));
		createTextButton(Text.literal("> Visual Shields"), (button -> toggleVisualShields()));*/
	}

	private void toggleLeaveBehind() {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeUuid(tardis().getUuid());
		buf.writeBoolean(!PropertiesHandler.getBool(tardis().getHandlers().getProperties(), PropertiesHandler.LEAVE_BEHIND));

		ClientPlayNetworking.send(PropertiesHandler.LEAVEBEHIND, buf);
		updateTardis();
	}

	private void toggleHostileAlarms() {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeUuid(tardis().getUuid());
		buf.writeBoolean(!PropertiesHandler.getBool(tardis().getHandlers().getProperties(), PropertiesHandler.HOSTILE_PRESENCE_TOGGLE));

		ClientPlayNetworking.send(PropertiesHandler.HOSTILEALARMS, buf);
		updateTardis();
	}

	/*private void toggleShields() {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeUuid(tardis().getUuid());
		buf.writeBoolean(!PropertiesHandler.getBool(tardis().getHandlers().getProperties(), ShieldData.IS_SHIELDED));

		ClientPlayNetworking.send(PropertiesHandler.SHIELDS, buf);
		updateTardis();
	}

	private void toggleVisualShields() {
		PacketByteBuf buf = PacketByteBufs.create();

		buf.writeUuid(tardis().getUuid());
		buf.writeBoolean(!PropertiesHandler.getBool(tardis().getHandlers().getProperties(), ShieldData.IS_VISUALLY_SHIELDED));

		ClientPlayNetworking.send(PropertiesHandler.VISUAL_SHIELDS, buf);
		updateTardis();
	}*/

	private <T extends ClickableWidget> void addButton(T button) {
		this.addDrawableChild(button);
		button.active = true; // this whole method is unnecessary bc it defaults to true ( ?? )
		this.buttons.add((ButtonWidget) button);
	}

	// this might be useful, so remember this exists and use it later on ( although its giving NTM vibes.. )
	private void createTextButton(Text text, ButtonWidget.PressAction onPress) {
		this.addButton(
				new PressableTextWidget(
						(int) (left + (bgWidth * 0.06f)),
						(int) (top + (bgHeight * (0.1f * (choicesCount + 1)))),
						this.textRenderer.getWidth(text),
						10,
						text,
						onPress,
						this.textRenderer
				)
		);

		choicesCount++;
	}

	public void backToExteriorChangeScreen() {
		MinecraftClient.getInstance().setScreen(this.parent);
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		this.drawBackground(context);
		context.drawText(this.textRenderer, Text.literal(": " + (PropertiesHandler.getBool(this.tardis().getHandlers().getProperties(), PropertiesHandler.LEAVE_BEHIND) ? "ON" : "OFF")), (int) (left + (bgWidth * 0.46f)), (int) (top + (bgHeight * (0.1f * 2))), Color.ORANGE.getRGB(), false);
		context.drawText(this.textRenderer, Text.literal(": " + (PropertiesHandler.getBool(this.tardis().getHandlers().getProperties(), PropertiesHandler.HOSTILE_PRESENCE_TOGGLE) ? "ON" : "OFF")), (int) (left + (bgWidth * 0.48f)), (int) (top + (bgHeight * (0.1f * 3))), Color.ORANGE.getRGB(), false);
		//context.drawText(this.textRenderer, Text.literal(": " + (PropertiesHandler.getBool(this.tardis().getHandlers().getProperties(), ShieldData.IS_SHIELDED) ? "ON" : "OFF")), (int) (left + (bgWidth * 0.3f)), (int) (top + (bgHeight * (0.1f * 4))), Color.ORANGE.getRGB(), false);
		//context.drawText(this.textRenderer, Text.literal(": " + (PropertiesHandler.getBool(this.tardis().getHandlers().getProperties(), ShieldData.IS_VISUALLY_SHIELDED) ? "ON" : "OFF")), (int) (left + (bgWidth * 0.46f)), (int) (top + (bgHeight * (0.1f * 5))), Color.ORANGE.getRGB(), false);
		//
		context.drawText(this.textRenderer, Text.literal("Date created:"), (int) (left + (bgWidth * 0.06f)), (int) (top + (bgHeight * (0.1f * 5))), 0xadcaf7, false);
		context.drawText(this.textRenderer, Text.literal(this.tardis().getHandlers().getStats().getCreationString()), (int) (left + (bgWidth * 0.06f)), (int) (top + (bgHeight * (0.1f * 6))), 0xadcaf7, false);
		super.render(context, mouseX, mouseY, delta);
	}

	private void drawBackground(DrawContext context) {
		context.drawTexture(TEXTURE, left, top, 0, 0, bgWidth, bgHeight);
	}
}