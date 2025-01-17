package loqor.ait.tardis.data;

import loqor.ait.core.AITItems;
import loqor.ait.core.util.DeltaTimeManager;
import loqor.ait.core.util.TimeUtil;
import loqor.ait.AITMod;
import loqor.ait.core.AITSounds;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.util.AbsoluteBlockPos;
import loqor.ait.tardis.util.TardisUtil;
import loqor.ait.tardis.wrapper.server.ServerTardis;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.DustColorTransitionParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;


public class TardisCrashData extends TardisLink {
	public static final String TARDIS_RECOVERY_STATE = "tardis_recovery_state";
	public static final String TARDIS_REPAIR_TICKS = "tardis_recovery_ticks";

	private static final String DELAY_ID_START = AITMod.MOD_ID + "-tardiscrashrecoverydelay-";
	public static final Integer UNSTABLE_TICK_START_THRESHOLD = 2_400;
	public static final Integer MAX_REPAIR_TICKS = 7_000;

	public boolean isToxic() {
		return this.getState() == State.TOXIC;
	}

	public boolean isUnstable() {
		return this.getState() == State.UNSTABLE;
	}

	@Override
	public void tick(MinecraftServer server) {
		super.tick(server);
		if (this.findTardis().isEmpty()) return;
		if (PropertiesHandler.get(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE) == null) {
			PropertiesHandler.set(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE, State.NORMAL);
		}

		if (getRepairTicks() > 0) {
			setRepairTicks(this.findTardis().get().isRefueling() ? getRepairTicks() - 10 : getRepairTicks() - 1);
		}
		if (getRepairTicks() <= 0 && State.NORMAL == getState()) return;
		ServerTardis tardis = (ServerTardis) this.findTardis().get();
		if (getRepairTicks() <= 0) {
			setState(State.NORMAL);
			tardis.getHandlers().getAlarms().disable();
			return;
		}
		if (getState() != State.NORMAL) {
			tardis.getHandlers().getAlarms().enable();
		}
		if (getRepairTicks() < UNSTABLE_TICK_START_THRESHOLD && State.UNSTABLE != getState() && getRepairTicks() > 0) {
			setState(State.UNSTABLE);
			tardis.getHandlers().getAlarms().disable();
		}
		AbsoluteBlockPos.Directed exteriorPosition = tardis.getTravel().getExteriorPos();
		double x = directionToInteger(exteriorPosition.getDirection())[0];
		double z = directionToInteger(exteriorPosition.getDirection())[1];
		ServerWorld exteriorWorld = (ServerWorld) exteriorPosition.getWorld();
		if (tardis.getDoor().isOpen() && this.getState() != State.NORMAL) {
			exteriorWorld.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
					exteriorPosition.toCenterPos().x, exteriorPosition.getY() + 2f,
					exteriorPosition.toCenterPos().z,
					8,
					0.05D, 0.05D, 0.05D, 0.01D
			);
		}
		if (getState() != State.TOXIC) return;
		if (DeltaTimeManager.isStillWaitingOnDelay(DELAY_ID_START + tardis.getUuid().toString())) return;
		exteriorWorld.spawnParticles(new DustColorTransitionParticleEffect(
						new Vector3f(0.75f, 0.85f, 0.75f), new Vector3f(0.15f, 0.25f, 0.15f), 3),
				exteriorPosition.toCenterPos().x, exteriorPosition.getY() + 0.1f,
				exteriorPosition.toCenterPos().z,
				25,
				0.05D, 0.75D, 0.05D, 0.01D
		);
		if (DeltaTimeManager.isStillWaitingOnDelay(DELAY_ID_START + tardis.getUuid().toString())) return;
		if (!TardisUtil.isInteriorNotEmpty(tardis)) return;
		for (ServerPlayerEntity serverPlayerEntity : TardisUtil.getPlayersInInterior(tardis)) {
			if (serverPlayerEntity.getEquippedStack(EquipmentSlot.HEAD).getItem() == AITItems.RESPIRATOR || serverPlayerEntity.getEquippedStack(EquipmentSlot.HEAD).getItem() == AITItems.FACELESS_RESPIRATOR) continue;
			serverPlayerEntity.playSound(AITSounds.CLOISTER, 1f, 1f);
			serverPlayerEntity.damage(exteriorWorld.getDamageSources().magic(), 3f);
			//TODO this messes with people and specifically me so im gonna remove it for now serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 100, 5, true, false, false));
			serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 100, 3, true, false, false));
			serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 100, 5, true, false, false));
		}
		DeltaTimeManager.createDelay(DELAY_ID_START + tardis.getUuid().toString(), (long) TimeUtil.secondsToMilliseconds(2));
	}

	public TardisCrashData(Tardis tardis) {
		super(tardis, "crash");

		if (findTardis().isEmpty()) {
		}
        /*if (PropertiesHandler.get(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE) == null) {
            PropertiesHandler.set(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE, State.NORMAL);
        }*/
	}

	public State getState() {
		if (findTardis().isEmpty() || PropertiesHandler.get(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE) == null)
			return State.NORMAL;
		if (PropertiesHandler.get(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE) instanceof State)
			return (State) PropertiesHandler.get(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE);
		return State.valueOf((String) PropertiesHandler.get(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE));
	}

	public void setState(State state) {
		if (findTardis().isEmpty()) return;
		PropertiesHandler.set(findTardis().get().getHandlers().getProperties(), TARDIS_RECOVERY_STATE, state);
	}

	public Integer getRepairTicks() { // what kind of madman uses Integer instead of int
		if (findTardis().isEmpty()) return 0;
		return PropertiesHandler.getInt(findTardis().get().getHandlers().getProperties(), TARDIS_REPAIR_TICKS);
	}

	public int getRepairTicksAsSeconds() {
		return (this.getRepairTicks() / 20) / 10;
	}

	public void setRepairTicks(Integer ticks) {
		if (findTardis().isEmpty()) return;
		if (ticks > MAX_REPAIR_TICKS) {
			setRepairTicks(MAX_REPAIR_TICKS);
			return;
		}
		PropertiesHandler.set(findTardis().get().getHandlers().getProperties(), TARDIS_REPAIR_TICKS, ticks);
	}

	public void addRepairTicks(Integer ticks) {
		if (findTardis().isEmpty()) return;
		PropertiesHandler.set(findTardis().get().getHandlers().getProperties(), TARDIS_REPAIR_TICKS, getRepairTicks() + ticks);
	}

	public double[] directionToInteger(Direction direction) {
		return switch (direction) {
			default -> new double[]{0.5d, 0.5d};
			case EAST -> new double[]{-0.5d, 0.5d};
			case SOUTH -> new double[]{-0.5d, -0.5d};
			case WEST -> new double[]{0.5d, -0.5d};
		};
	}

	public enum State {
		NORMAL,
		UNSTABLE,
		TOXIC
	}
}
