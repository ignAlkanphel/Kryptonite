package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public record EquipmentCondition(ItemFilter itemFilter, EquipmentSlotGroup equipmentSlot) implements Condition {

    public static final MapCodec<EquipmentCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemFilter.CODEC.fieldOf("item_filters").forGetter(EquipmentCondition::itemFilter),
            EquipmentSlotGroup.CODEC.fieldOf("equipment_slot").forGetter(EquipmentCondition::equipmentSlot)
    ).apply(instance, EquipmentCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, EquipmentCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodecWithRegistriesTrusted(ItemFilter.CODEC), EquipmentCondition::itemFilter,
            EquipmentSlotGroup.STREAM_CODEC, EquipmentCondition::equipmentSlot,
            EquipmentCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        if (!(context.getEntity() instanceof LivingEntity livingEntity)) return false;

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (equipmentSlot.test(slot) && itemFilter.test(livingEntity.level(), livingEntity.getItemBySlot(slot))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer<EquipmentCondition> getSerializer() {
        return KryptoniteConditionSerializers.EQUIPMENT.get();
    }

    public static class Serializer extends ConditionSerializer<EquipmentCondition> {

        @Override
        public MapCodec<EquipmentCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, EquipmentCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Equipment")
                    .setDescription("Checks if the entity has the specified item equipped in the specified equipment slot.")
                    .add("item_filters", ItemFilter.documentation(provider), "Item filter(s) that the equipped item must pass.")
                    .add("equipment_slot", KryptoniteDocumented.TYPE_EQUIPMENT_SLOT_GROUP, "The equipment slot to check for the item.")
                    .addExampleObject(new EquipmentCondition(ItemFilter.EMPTY, EquipmentSlotGroup.MAINHAND));
        }
    }

}