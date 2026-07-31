package net.alkanphel.kryptonite.util.apoli;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import net.alkanphel.kryptonite.mixin.common.SlotRangesAccessor;
import net.alkanphel.kryptonite.power.compat.curios.KryptoniteCuriosCompat;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class InventoryUtil {

    public enum ProcessMode implements StringRepresentable, ToIntFunction<ItemStack> {
        STACKS {
            @Override
            public int applyAsInt(ItemStack value) {
                return 1;
            }
        },
        ITEMS {
            @Override
            public int applyAsInt(ItemStack value) {
                return value.getCount();
            }
        };

        public static final Codec<ProcessMode> CODEC = StringRepresentable.fromEnum(ProcessMode::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

    public static int checkInventory(Entity entity, IntCollection slots, Optional<ItemCondition> itemCondition, ProcessMode processMode) {
        IntSet preppedSlots = prepSlots(slots, entity);
        int matches = 0;

        for (int preppedSlot : preppedSlots) {
            SlotAccess slotAccess = getStackReference(entity, preppedSlot);

            ItemStack stack = slotAccess.get();
            if (stack.isEmpty()) continue;

            if (itemCondition.map(condition -> condition.test(entity.level(), stack)).orElse(true)) {
                matches += processMode.applyAsInt(stack);
            }
        }

        return matches;
    }

    public static int checkCuriosInventory(LivingEntity entity, List<String> curiosSlots, Optional<ItemCondition> itemCondition, ProcessMode processMode) {
        List<String> slotsToCheck = curiosSlots.isEmpty()
                ? KryptoniteCuriosCompat.INSTANCE.getSlots(entity.level())
                : curiosSlots;

        int matches = 0;

        for (String slot : slotsToCheck) {
            for (ItemStack stack : KryptoniteCuriosCompat.INSTANCE.getFromSlot(entity, slot)) {
                if (stack.isEmpty()) continue;

                if (itemCondition.map(condition -> condition.test(entity.level(), stack)).orElse(true)) {
                    matches += processMode.applyAsInt(stack);
                }
            }
        }

        return matches;
    }

    public static void forEachStack(Entity entity, Consumer<ItemStack> stackConsumer) {
        OptionalInt slotToSkip = getSelectedHotBarSlot(entity);
        for (int slot : getAllSlots()) {

            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            SlotAccess slotAccess = entity.getSlot(slot);
            if (slotAccess == null) continue;

            ItemStack stack = slotAccess.get();

            if (!stack.isEmpty()) {
                stackConsumer.accept(stack);
            }
        }
    }

    public static @Nullable SlotAccess getStackReferenceFromStack(Entity entity, ItemStack stack) {
        return getStackReferenceFromStack(entity, stack, (provStack, refStack) -> provStack == refStack);
    }

    public static @Nullable SlotAccess getStackReferenceFromStack(Entity entity, ItemStack stack, BiPredicate<ItemStack, ItemStack> equalityPredicate) {
        OptionalInt slotToSkip = getSelectedHotBarSlot(entity);
        for (int slot : getAllSlots()) {
            if (slotToSkip.isPresent() && slotToSkip.getAsInt() == slot) {
                continue;
            }

            SlotAccess slotAccess = entity.getSlot(slot);
            if (slotAccess != null && equalityPredicate.test(stack, slotAccess.get())) {
                return slotAccess;
            }
        }

        return null;
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
     *  Checks whether the specified {@code slot} index is within the bounds of the entity's {@link SlotAccess}.
     *  @param entity the entity to check the bounds of its {@link SlotAccess}
     *  @param slot the slot index
     *  @return {@code true} if the slot index is within the bounds
     */
    public static boolean slotWithinBounds(Entity entity, int slot) {
        return entity.getSlot(slot) != null;
    }

    public static SlotAccess getStackReference(@NotNull Entity entity, int slot) {
        return entity.getSlot(slot);
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

        return IntSets.unmodifiable(ALL_SLOTS);
    }

    public static IntSet prepSlots(IntCollection slots, Entity entity) {
        IntStream slotStream = slots.isEmpty()
                ? getAllSlots().intStream()
                : slots.intStream();

        return slotStream
                .filter(slot -> slotWithinBounds(entity, slot))
                .collect(IntOpenHashSet::new, IntOpenHashSet::add, IntOpenHashSet::addAll);
    }

    public static OptionalInt getSlotFromStackReference(Entity entity, SlotAccess slotAccess) {
        for (int slot : getAllSlots()) {
            SlotAccess queriedSlotAccess = entity.getSlot(slot);
            if (queriedSlotAccess != null && queriedSlotAccess.equals(slotAccess)) {
                return OptionalInt.of(slot);
            }
        }

        return OptionalInt.empty();
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

    public static IntSet toSlotIdSet(Collection<SlotRange> slotRanges) {
        IntSet slotIdSet = new IntOpenHashSet();
        for (SlotRange slotRange : slotRanges) {
            slotIdSet.addAll(slotRange.slots());
        }

        return slotIdSet;
    }

}