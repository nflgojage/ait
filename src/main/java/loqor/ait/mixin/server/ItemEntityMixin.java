package loqor.ait.mixin.server;

import loqor.ait.core.AITDimensions;
import loqor.ait.core.item.SiegeTardisItem;
import loqor.ait.tardis.Tardis;
import loqor.ait.tardis.util.TardisUtil;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// mmm mixin i love mixin
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

	@Inject(method = "tick", at = @At("HEAD"))
	private void ait$tick(CallbackInfo ci) {
		ItemEntity entity = (ItemEntity) (Object) this;
		ItemStack stack = entity.getStack();

		if (entity.getWorld().isClient()) return;

		if (stack.getItem() instanceof SiegeTardisItem) {
			Tardis found = SiegeTardisItem.getTardis(stack);

			if (found == null) return;
			// kill ourselves and place down the exterior
			SiegeTardisItem.placeTardis(found, SiegeTardisItem.fromEntity(entity));
			entity.kill();
		}
		// if entity is in tardis and y is less than -100 save them
		if (entity.getY() <= -100 && entity.getWorld().getRegistryKey().equals(AITDimensions.TARDIS_DIM_WORLD)) {
			Tardis found = TardisUtil.findTardisByInterior(entity.getBlockPos(), true);

			if (found == null) return;
			TardisUtil.teleportInside(found, entity);
		}
	}
}
