package net.alkanphel.kryptonite.util.apoli;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.alkanphel.kryptonite.mixin.common.SlotRangesAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;
import java.util.function.Consumer;

public class InventoryUtil {

    public static void forEachStack(Entity entity, Consumer<ItemStack> stackConsumer) {
        OptionalInt slotToSkip = getSelectedHotBarSlot(entity);
        for (int slot : getAllSlots()) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            SlotAccess stackReference = entity.getSlot(slot);
            if (stackReference == null) continue;

            ItemStack stack = stackReference.get();

            if (!stack.isEmpty()) {
                stackConsumer.accept(stack);
            }
        }
    }

    /**
     * <p>For players, their selected hotbar slot will overlap with the `weapon.mainhand` slot reference. This
     * method returns the slot ID of the selected hotbar slot.</p>
     *
     * @param entity The entity to get the slot ID of its selected hotbar slot
     * @return The slot ID of the hotbar slot or {@link Integer#MIN_VALUE} if the entity is not a player
     */
    private static OptionalInt getSelectedHotBarSlot(Entity entity) {
        SlotRange slotRange = entity instanceof Player player
                ? SlotRanges.nameToIds("hotbar." + player.getInventory().getSelectedSlot())
                : null;

        return slotRange != null
                ? OptionalInt.of(slotRange.slots().getFirst())
                : OptionalInt.empty();
    }

    /**
     * Creates a stack reference that is not linked to any entity for use with item actions.
     * <p>
     * Recommended for usage when either you don't have an entity for this operation, or you don't want to set the entity's StackReference.
     *
     * @param startingStack The ItemStack that this reference will start with.
     * @return A {@linkplain SlotAccess} that contains an ItemStack.
     */
    public static SlotAccess createStackReference(ItemStack startingStack) {
        return new SlotAccess() {
            ItemStack stack = startingStack;

            @Override
            public ItemStack get() {
                return stack;
            }

            @Override
            public boolean set(ItemStack stack) {
                this.stack = stack;
                return true;
            }
        };
    }

    private static final IntSet ALL_SLOTS = new IntOpenHashSet();

    public static IntSet getAllSlots() {
        if (ALL_SLOTS.isEmpty()) {
            for (SlotRange slotRange : SlotRangesAccessor.kryptonite$getSlotRanges()) {
                ALL_SLOTS.addAll(slotRange.slots());
            }
        }

        return ALL_SLOTS;
    }


    // ------------------------------------------------------------------------------------------------------------------------


    public static OptionalInt getSpaceInInventory(Player player, ItemStack stack) {
        return getSpaceInInventory(player.getInventory(), stack);
    }

    public static OptionalInt getSpaceInInventory(Inventory inventory, ItemStack stack) {
        int slot = inventory.getSlotWithRemainingSpace(stack);
        if (slot == -1) slot = inventory.getFreeSlot();

        return slot == -1 ? OptionalInt.empty() : OptionalInt.of(slot);
    }

    public static boolean hasSpaceInInventory(Player player, ItemStack stack) {
        return getSpaceInInventory(player, stack).isPresent();
    }

    public static boolean hasSpaceInInventory(Inventory playerInventory, ItemStack stack) {
        return getSpaceInInventory(playerInventory, stack).isPresent();
    }

}