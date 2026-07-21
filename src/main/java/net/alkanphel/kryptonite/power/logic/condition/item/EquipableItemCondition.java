package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.equipment.Equippable;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

import java.util.Optional;

public record EquipableItemCondition(Optional<EquipmentSlotGroup> equipmentSlot) implements ItemCondition {

    public static final MapCodec<EquipableItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EquipmentSlotGroup.CODEC.optionalFieldOf("equipment_slot").forGetter(EquipableItemCondition::equipmentSlot)
    ).apply(instance, EquipableItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipableItemCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(EquipmentSlotGroup.STREAM_CODEC), EquipableItemCondition::equipmentSlot,
            EquipableItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        Equippable equippable = context.stack().get(DataComponents.EQUIPPABLE);
        if (equippable == null) return false;

        return equipmentSlot
                .map(slot -> slot.test(equippable.slot()))
                .orElse(true);
    }

    @Override
    public ItemConditionSerializer<EquipableItemCondition> getSerializer() {
        return ItemConditionSerializers.EQUIPABLE.get();
    }

    public static class Serializer extends ItemConditionSerializer<EquipableItemCondition> {

        @Override
        public MapCodec<EquipableItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, EquipableItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Equipable")
                    .setDescription("Checks if the item is able to be equipped.")
                    .addOptional("equipment_slot", KryptoniteDocumented.TYPE_EQUIPMENT_SLOT_GROUP, "If specified, checks if the item is equipable in the specified equipment slot.")
                    .addExampleObject(new EquipableItemCondition(Optional.empty()))
                    .addExampleObject(new EquipableItemCondition(Optional.of(EquipmentSlotGroup.CHEST)));
        }
    }

}