package loqor.ait.core.blockentities;

import io.wispforest.owo.util.ImplementedInventory;
import loqor.ait.AITMod;
import loqor.ait.core.AITBlockEntityTypes;
import loqor.ait.core.screen_handlers.EngineScreenHandler;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.control.impl.SecurityControl;
import loqor.ait.tardis.data.properties.PropertiesHandler;
import loqor.ait.tardis.link.LinkableBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static loqor.ait.tardis.util.TardisUtil.findTardisByInterior;

public class EngineBlockEntity extends LinkableBlockEntity implements NamedScreenHandlerFactory, ImplementedInventory, SidedInventory {

	public static final DefaultedList<ItemStack> items = DefaultedList.ofSize(2, ItemStack.EMPTY);

	public EngineBlockEntity(BlockPos pos, BlockState state) {
		super(AITBlockEntityTypes.ENGINE_BLOCK_ENTITY_TYPE, pos, state);
	}

	public void useOn(BlockState state, World world, boolean sneaking, PlayerEntity player) {
		if (world.isClient() || this.findTardis().isEmpty()) return;
		boolean security = PropertiesHandler.getBool(this.findTardis().get().getHandlers().getProperties(), SecurityControl.SECURITY_KEY);
		if (security) {
			if (!SecurityControl.hasMatchingKey((ServerPlayerEntity) player, this.findTardis().get())) {
				return;
			}
		}
		player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
		NamedScreenHandlerFactory screenHandlerFactory = state.createScreenHandlerFactory(world, pos);
		if (screenHandlerFactory != null) {
			player.openHandledScreen(screenHandlerFactory);
		}
	}


	@Override
	public Optional<Tardis> findTardis() {
		if (this.tardisId == null && this.hasWorld()) {
			assert this.getWorld() != null;
			Tardis found = findTardisByInterior(pos, !this.getWorld().isClient());
			if (found != null)
				this.setTardis(found);
		}
		return super.findTardis();
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		Inventories.readNbt(nbt, items);
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		Inventories.writeNbt(nbt, items);
		super.writeNbt(nbt);
	}

	@Override
	public DefaultedList<ItemStack> getItems() {
		return items;
	}

	@Override
	public int[] getAvailableSlots(Direction side) {
		int[] result = new int[getItems().size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = i;
		}
		return result;
	}

	@Override
	public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
		return dir != Direction.UP;
	}

	@Override
	public boolean canExtract(int slot, ItemStack stack, Direction dir) {
		return true;
	}

	@Override
	public Text getDisplayName() {
		return Text.literal("idk");
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
		return new EngineScreenHandler(syncId, playerInventory, this);
	}
}
