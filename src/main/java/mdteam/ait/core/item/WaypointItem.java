package mdteam.ait.core.item;

import mdteam.ait.core.AITItems;
import mdteam.ait.core.blockentities.ConsoleBlockEntity;
import mdteam.ait.tardis.Tardis;
import mdteam.ait.tardis.util.AbsoluteBlockPos;
import mdteam.ait.tardis.util.Waypoint;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static mdteam.ait.tardis.control.impl.DimensionControl.convertWorldValueToModified;

public class WaypointItem extends Item {
    public static final String POS_KEY = "pos";
    /*public static final String LOCKED_KEY = "locked";*/
    // fixme ehhhhh should we have a locked variable for the tardis waypoints? maybe it could be helpful?

    public WaypointItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        ItemStack itemStack = context.getStack();
        Hand hand = context.getHand();

        if (player == null)
            return ActionResult.FAIL;
        if (world.isClient()) return ActionResult.SUCCESS;

        if (!player.isSneaking()) return ActionResult.FAIL;
        if (hand != Hand.MAIN_HAND) return ActionResult.FAIL;
        if (!(world.getBlockEntity(pos) instanceof ConsoleBlockEntity console)) return ActionResult.FAIL;

        if (console.getTardis() == null || console.getTardis().getTravel().getPosition() == null)
            return ActionResult.PASS;

        if (getPos(itemStack) == null) setPos(itemStack, console.getTardis().getTravel().getPosition());

        console.getTardis().getHandlers().getWaypoints().markHasCartridge();
        console.getTardis().getHandlers().getWaypoints().set(Waypoint.fromDirected(getPos(itemStack)), true);
        player.setStackInHand(hand, ItemStack.EMPTY);

        world.playSound(null, pos, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 6f, 1);

        return ActionResult.SUCCESS;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        if (!Screen.hasShiftDown()) {
            tooltip.add(Text.translatable("tooltip.ait.remoteitem.holdformoreinfo").formatted(Formatting.GRAY).formatted(Formatting.ITALIC));
            return;
        }

        AbsoluteBlockPos.Directed pos = getPos(stack);
        if (pos == null) return;

        tooltip.add(Text.translatable("waypoint.position.tooltip").append(Text.literal(
                " > " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()))
                .formatted(Formatting.BLUE));
        tooltip.add(Text.translatable("waypoint.direction.tooltip").append(Text.literal(
            " > " + pos.getDirection().asString().toUpperCase()))
                .formatted(Formatting.BLUE));
        tooltip.add(Text.translatable("waypoint.dimension.tooltip").append(Text.literal(
            " > " + convertWorldValueToModified(pos.getDimension().getValue())))
                .formatted(Formatting.BLUE));
    }

    public static ItemStack create(AbsoluteBlockPos.Directed pos) {
        ItemStack stack = new ItemStack(AITItems.WAYPOINT_CARTRIDGE);
        setPos(stack, pos);
        return stack;
    }

    public static AbsoluteBlockPos.Directed getPos(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();

        if (!nbt.contains(POS_KEY)) return null;

        System.out.println(nbt);

        return AbsoluteBlockPos.Directed.fromNbt(nbt.getCompound(POS_KEY));
    }
    public static void setPos(ItemStack stack, AbsoluteBlockPos.Directed pos) {
        NbtCompound nbt = stack.getOrCreateNbt();

        nbt.put(POS_KEY, pos.toNbt());

        System.out.println(pos);
        System.out.println(getPos(stack));
    }
    public static boolean hasPos(ItemStack stack) {
        return stack.getOrCreateNbt().contains(POS_KEY);
    }
}
