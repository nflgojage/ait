package loqor.ait.tardis.wrapper.client.manager;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.GsonBuilder;
import loqor.ait.AITMod;
import loqor.ait.client.sounds.ClientSoundManager;
import loqor.ait.core.blockentities.ConsoleBlockEntity;
import loqor.ait.core.blockentities.DoorBlockEntity;
import loqor.ait.core.blockentities.ExteriorBlockEntity;
import loqor.ait.tardis.*;
import loqor.ait.tardis.data.SonicHandler;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.wrapper.server.manager.ServerTardisManager;
import loqor.ait.tardis.data.DoorData;
import loqor.ait.tardis.util.AbsoluteBlockPos;
import loqor.ait.tardis.util.SerialDimension;
import loqor.ait.tardis.wrapper.client.ClientTardis;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import java.util.*;
import java.util.function.Consumer;

public class ClientTardisManager extends TardisManager<ClientTardis> {

	public static final Identifier ASK = new Identifier("ait", "ask_tardis");
	public static final Identifier ASK_POS = new Identifier("ait", "ask_pos_tardis");
	public static final Identifier LET_KNOW_UNLOADED = new Identifier("ait", "let_know_unloaded");
	private static ClientTardisManager instance;

	// Are all these maps necessary? What are they even for??
	public final Map<ConsoleBlockEntity, Tardis> consoleToTardis = new HashMap<>();
	public final Map<ExteriorBlockEntity, Tardis> exteriorToTardis = new HashMap<>();
	public final Map<DoorBlockEntity, Tardis> interiorDoorToTardis = new HashMap<>();
	public final List<UUID> loadedTardises = new ArrayList<>();
	private final Multimap<UUID, Consumer<ClientTardis>> subscribers = ArrayListMultimap.create();

	public ClientTardisManager() {
		ClientPlayNetworking.registerGlobalReceiver(ServerTardisManager.SEND,
				(client, handler, buf, responseSender) -> ClientTardisManager.getInstance().sync(buf)
		);

		ClientPlayNetworking.registerGlobalReceiver(ServerTardisManager.UPDATE,
				(client, handler, buf, responseSender) -> ClientTardisManager.getInstance().update(buf));

		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
	}

	@Override
	public void loadTardis(UUID uuid, Consumer<ClientTardis> consumer) {
		PacketByteBuf data = PacketByteBufs.create();
		data.writeUuid(uuid);

		ClientTardisManager.getInstance().subscribers.put(uuid, consumer);

		if (MinecraftClient.getInstance().getNetworkHandler() == null) return;

		ClientPlayNetworking.send(ASK, data);
	}

	/**
	 * Asks the server for a tardis at an exterior position.
	 *
	 * @param pos
	 */
	public void askTardis(AbsoluteBlockPos pos) {
		PacketByteBuf data = PacketByteBufs.create();
		data.writeNbt(pos.toNbt());

		ClientPlayNetworking.send(ASK_POS, data);
	}

	private void sync(UUID uuid, String json) {
		ClientTardis tardis = ClientTardisManager.getInstance().gson.fromJson(json, ClientTardis.class);

		synchronized (this) {
			ClientTardisManager.getInstance().getLookup().put(uuid, tardis);
			AITMod.LOGGER.info("RECIEVED TARDIS: " + uuid);

			for (Consumer<ClientTardis> consumer : ClientTardisManager.getInstance().subscribers.removeAll(uuid)) {
				consumer.accept(tardis);
			}
		}
	}

	private void sync(UUID uuid, PacketByteBuf buf) {
		ClientTardisManager.getInstance().sync(uuid, buf.readString());
	}

	private void sync(PacketByteBuf buf) {
		ClientTardisManager.getInstance().sync(buf.readUuid(), buf);
	}

	private void update(ClientTardis tardis, String header, String json) {
		AITMod.LOGGER.info("Updating " + header); // remove this
		switch (header) {
			case "desktop" ->
					tardis.setDesktop(ClientTardisManager.getInstance().gson.fromJson(json, TardisDesktop.class));
			case "door" -> tardis.setDoor(ClientTardisManager.getInstance().gson.fromJson(json, DoorData.class));
			case "exterior" ->
					tardis.setExterior(ClientTardisManager.getInstance().gson.fromJson(json, TardisExterior.class));
			case "travel" ->
					tardis.setTravel(ClientTardisManager.getInstance().gson.fromJson(json, TardisTravel.class));
			case "sonic" ->
					tardis.setSonic(ClientTardisManager.getInstance().gson.fromJson(json, SonicHandler.class));
		}
	}

	private void updateProperties(ClientTardis tardis, String key, String type, String value) {
		//AITMod.LOGGER.info("Updating Properties " + key + " to " + value); // remove this
		switch (type) {
			case "string" -> PropertiesHandler.set(tardis, key, value, false);
			case "boolean" -> PropertiesHandler.set(tardis, key, Boolean.parseBoolean(value), false);
			case "int" -> PropertiesHandler.set(tardis, key, Integer.parseInt(value), false);
			case "double" -> PropertiesHandler.set(tardis, key, Double.parseDouble(value), false);
			case "float" -> PropertiesHandler.set(tardis, key, Float.parseFloat(value), false);
			case "identifier" -> PropertiesHandler.set(tardis, key, new Identifier(value), false);
		}
	}

	private void update(UUID uuid, PacketByteBuf buf) {
		if (!ClientTardisManager.getInstance().getLookup().containsKey(uuid)) {
			ClientTardisManager.getInstance().getTardis(uuid, t -> {
			}); // force ASK
			return;
		}

		ClientTardis tardis = ClientTardisManager.getInstance().getLookup().get(uuid);
		String header = buf.readString();

		if (header.equals("properties")) {
			ClientTardisManager.getInstance().updateProperties(tardis, buf.readString(), buf.readString(), buf.readString());
			return;
		}

		String json = buf.readString();

		ClientTardisManager.getInstance().update(tardis, header, json);
	}

	private void update(PacketByteBuf buf) {
		ClientTardisManager.getInstance().update(buf.readUuid(), buf);
	}

	@Override
	public GsonBuilder getGsonBuilder(GsonBuilder builder) {
		builder.registerTypeAdapter(SerialDimension.class, new SerialDimension.ClientSerializer());
		return builder;
	}

	public static void init() {
		instance = new ClientTardisManager();
	}

	// Replace with loadTardis
	@Deprecated
	public void ask(UUID uuid) {
		PacketByteBuf data = PacketByteBufs.create();
		data.writeUuid(uuid);

		ClientPlayNetworking.send(ASK, data);
	}

	public void letKnowUnloaded(UUID uuid) {
		ClientPlayNetworking.send(LET_KNOW_UNLOADED, PacketByteBufs.create().writeUuid(uuid));
	}

	private void onTick(MinecraftClient client) {
		if (client.player == null || client.world == null) return;

		for (Tardis tardis : ClientTardisManager.getInstance().getLookup().values()) {
			tardis.tick(client);
		}

		ClientSoundManager.tick(client);
	}

	@Override
	public void reset() {
		ClientTardisManager.getInstance().subscribers.clear();
		super.reset();
	}

	public static ClientTardisManager getInstance() {
		return instance;
	}

	@Override
	public Map<UUID, ClientTardis> getLookup() {
		return super.getLookup();
	}
}
