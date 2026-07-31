package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.compat.curios.KryptoniteCuriosCompat;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.SlotRange;
import net.minecraft.world.inventory.SlotRanges;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class KeepInventoryAbility extends Ability {

    private static final ObjectOpenHashSet<Integer> DEFAULT_SLOTS = ObjectOpenHashSet.of(SlotRanges.nameToIds("inventory.*"), SlotRanges.nameToIds("hotbar.*"), SlotRanges.nameToIds("armor.*"), SlotRanges.nameToIds("weapon.*"))
            .stream()
            .map(SlotRange::slots)
            .map(IntCollection::intStream)
            .flatMap(IntStream::boxed)
            .collect(Collectors.toCollection(ObjectOpenHashSet::new));

    public static final MapCodec<KeepInventoryAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteCodecs.SLOT_RANGE_CODEC.listOf().optionalFieldOf("slots").forGetter(a -> a.slotRanges),
            Codec.STRING.listOf().optionalFieldOf("curios_slots").forGetter(a -> a.curiosSlots),
            ItemCondition.CODEC.optionalFieldOf("item_conditions").forGetter(a -> a.itemConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, KeepInventoryAbility::new));

    public final Optional<List<SlotRange>> slotRanges;
    private final Optional<List<String>> curiosSlots;
    private final Optional<ItemCondition> itemConditions;

    private final ObjectOpenHashSet<Integer> slots;
    private final Int2ObjectOpenHashMap<ItemStack> cachedStacks;

    public KeepInventoryAbility(Optional<List<SlotRange>> slotRanges, Optional<List<String>> curiosSlots, Optional<ItemCondition> itemConditions, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);

        this.slotRanges = slotRanges;
        this.curiosSlots = curiosSlots;
        this.itemConditions = itemConditions;

        this.slots = new ObjectOpenHashSet<>();
        this.cachedStacks = new Int2ObjectOpenHashMap<>();

        this.slotRanges.stream()
                .flatMap(Collection::stream)
                .map(SlotRange::slots)
                .map(IntCollection::intStream)
                .flatMap(IntStream::boxed)
                .forEach(this.slots::add);

        this.slots.trim();
    }

    public void preventItemsFromDropping(Player player) {
        ObjectOpenHashSet<Integer> slots = slotRanges
                .map(slotRanges -> this.slots)
                .orElse(DEFAULT_SLOTS);

        for (int slot : slots) {
            SlotAccess slotAccess = player.getSlot(slot);
            if (slotAccess == null) continue;

            ItemStack stack = slotAccess.get();

            if (!stack.isEmpty() && itemConditions.map(condition -> condition.test(player.level(), stack)).orElse(true)) {
                cachedStacks.put(slot, stack);
                slotAccess.set(ItemStack.EMPTY);
            }
        }

        preventCuriosFromDropping(player);
        this.cachedStacks.trim();
    }

    public void restoreSavedItems(Player player) {
        if (cachedStacks.isEmpty()) Kryptonite.LOGGER.warn("The \"kryptonite:keep_inventory\" ability tried restoring items without having saved any!");

        for (Int2ObjectMap.Entry<ItemStack> cachedStackEntry : cachedStacks.int2ObjectEntrySet()) {

            int slot = cachedStackEntry.getIntKey();
            ItemStack cachedStack = cachedStackEntry.getValue();

            if (!cachedStack.isEmpty()) {
                SlotAccess slotAccess = player.getSlot(slot);

                if (slotAccess != null) {
                    slotAccess.set(cachedStack);
                }
            }
        }

        this.cachedStacks.clear();
        this.cachedStacks.trim();
    }

    private void preventCuriosFromDropping(Player player) {
        List<String> slots = curiosSlots.orElseGet(() -> KryptoniteCuriosCompat.INSTANCE.getSlots(player.level()));
        KryptoniteCuriosCompat.INSTANCE.preventDropping(player, slots, stack -> itemConditions.map(condition -> condition.test(player.level(), stack)).orElse(true));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.KEEP_INVENTORY.get();
    }

    public static class Serializer extends AbilitySerializer<KeepInventoryAbility> {

        @Override
        public MapCodec<KeepInventoryAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, KeepInventoryAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents specific items in specific slots from dropping upon the player dying.")
                    .addOptional("slots", KryptoniteDocumented.TYPE_SLOT_RANGES, "If specified, only these slots are affected.")
                    .addOptional("curios_slots", TYPE_STRING, "If specified, only these slots from the Curios mod are affected (independently from the \"slots\" field).")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, only items that fulfill these conditions are affected.")
                    .addExampleObject(new KeepInventoryAbility(Optional.empty(), Optional.empty(), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new KeepInventoryAbility(Optional.of(List.of(Objects.requireNonNull(SlotRanges.nameToIds("hotbar.*")))), Optional.of(List.of("rings")), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}