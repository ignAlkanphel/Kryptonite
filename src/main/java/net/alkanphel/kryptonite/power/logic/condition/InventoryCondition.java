package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.item.ItemItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.alkanphel.kryptonite.util.apoli.InventoryUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.SlotRange;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.NumberComparator;

import java.util.List;
import java.util.Optional;

public class InventoryCondition implements Condition {

    public static final MapCodec<InventoryCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            InventoryUtil.ProcessMode.CODEC.optionalFieldOf("process_mode", InventoryUtil.ProcessMode.ITEMS).forGetter(a -> a.processMode),
            ItemCondition.CODEC.optionalFieldOf("item_conditions").forGetter(a -> a.itemConditions),
            KryptoniteCodecs.SLOT_RANGE_CODEC.listOf().optionalFieldOf("slots").forGetter(a -> a.slotRanges),
            Codec.STRING.listOf().optionalFieldOf("curios_slots").forGetter(a -> a.curiosSlots),
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_THAN).forGetter(a -> a.comparator),
            Codec.INT.optionalFieldOf("compare_to", 0).forGetter(a -> a.compareTo)
    ).apply(instance, InventoryCondition::new));

    private final InventoryUtil.ProcessMode processMode;
    private final Optional<ItemCondition> itemConditions;

    private final Optional<List<SlotRange>> slotRanges;
    private final IntSet slots;

    private final Optional<List<String>> curiosSlots;

    private final NumberComparator comparator;
    private final int compareTo;

    public InventoryCondition(InventoryUtil.ProcessMode processMode, Optional<ItemCondition> itemConditions, Optional<List<SlotRange>> slotRanges, Optional<List<String>> curiosSlots, NumberComparator comparator, int compareTo) {
        this.processMode = processMode;
        this.itemConditions = itemConditions;
        this.slotRanges = slotRanges;
        this.slots = slotRanges.map(InventoryUtil::toSlotIdSet).orElseGet(InventoryUtil::getAllSlots);
        this.curiosSlots = curiosSlots;
        this.comparator = comparator;
        this.compareTo = compareTo;
    }

    @Override
    public boolean test(DataContext context) {
        LivingEntity entity = context.getLivingEntity();
        if (entity == null) return false;

        int matches = 0;

        matches += InventoryUtil.checkInventory(entity, slots, itemConditions, processMode);

        if (curiosSlots.isPresent()) {
            matches += InventoryUtil.checkCuriosInventory(entity, curiosSlots.get(), itemConditions, processMode);
        }

        return comparator.compare(matches, compareTo);
    }

    @Override
    public ConditionSerializer<InventoryCondition> getSerializer() {
        return KryptoniteConditionSerializers.INVENTORY.get();
    }

    public static class Serializer extends ConditionSerializer<InventoryCondition> {

        @Override
        public MapCodec<InventoryCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, InventoryCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Inventory")
                    .setDescription("Checks the inventory of the entity.")
                    .addOptional("process_mode", SettingType.enumList(InventoryUtil.ProcessMode.values()), "Determines how the item stacks in the inventory are evaluated.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, only account for items from the inventory that fulfill these conditions.")
                    .addOptional("slots", KryptoniteDocumented.TYPE_SLOT_RANGES, "If specified, only items from these specified item slots are evaluated.")
                    .addOptional("curios_slots", TYPE_STRING, "If specified, these slots from the Curios mod are also evaluated (independently from the \"slots\" field). An empty list evaluates all Curios slots. If this field is not specified at all, Curios slots are not evaluated.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "Determines how the amount of items/stacks that were evaluated should be compared to the specified value.", NumberComparator.GREATER_THAN)
                    .addOptional("compare_to", TYPE_INT, "The value at which the amount of items/stacks that were evaluated will be compared to.", 0)
                    .addExampleObject(new InventoryCondition(InventoryUtil.ProcessMode.STACKS, Optional.empty(), Optional.empty(), Optional.empty(), NumberComparator.GREATER_OR_EQUAL, 10))
                    .addExampleObject(new InventoryCondition(InventoryUtil.ProcessMode.STACKS, Optional.empty(), Optional.empty(), Optional.of(List.of()), NumberComparator.GREATER_OR_EQUAL, 10))
                    .addExampleObject(new InventoryCondition(InventoryUtil.ProcessMode.ITEMS, Optional.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("dirt")))))), Optional.empty(), Optional.empty(), NumberComparator.GREATER_THAN, 16));
        }
    }

}