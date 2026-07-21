package net.alkanphel.kryptonite.power.logic.condition.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record DurabilityItemCondition(NumberComparator comparator, Value compareTo) implements ItemCondition {

    public static final MapCodec<DurabilityItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(DurabilityItemCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(DurabilityItemCondition::compareTo)
    ).apply(instance, DurabilityItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DurabilityItemCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, DurabilityItemCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), DurabilityItemCondition::compareTo,
            DurabilityItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        ItemStack stack = context.stack();
        return stack.isDamageableItem()
                && comparator.compare(Math.abs(stack.getMaxDamage() - stack.getDamageValue()), compareTo.getAsInt(null));
    }

    @Override
    public ItemConditionSerializer<DurabilityItemCondition> getSerializer() {
        return ItemConditionSerializers.DURABILITY.get();
    }

    public static class Serializer extends ItemConditionSerializer<DurabilityItemCondition> {

        @Override
        public MapCodec<DurabilityItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, DurabilityItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Durability")
                    .setDescription("Checks the current durability of the item")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new DurabilityItemCondition(NumberComparator.LESS_OR_EQUAL, new StaticValue(100)));
        }
    }

}