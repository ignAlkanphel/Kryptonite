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
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record AmountItemCondition(NumberComparator comparator, Value compareTo) implements ItemCondition {

    public static final MapCodec<AmountItemCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(AmountItemCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(AmountItemCondition::compareTo)
    ).apply(instance, AmountItemCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AmountItemCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, AmountItemCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), AmountItemCondition::compareTo,
            AmountItemCondition::new
    );

    @Override
    public boolean test(ItemConditionContext context) {
        return comparator.compare(context.stack().getCount(), compareTo.getAsInt(null));
    }

    @Override
    public ItemConditionSerializer<AmountItemCondition> getSerializer() {
        return ItemConditionSerializers.AMOUNT.get();
    }

    public static class Serializer extends ItemConditionSerializer<AmountItemCondition> {

        @Override
        public MapCodec<AmountItemCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<ItemCondition, AmountItemCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Amount")
                    .setDescription("Checks the amount of the item in the item stack.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used")
                    .add("compare_to", TYPE_VALUE, "The value that is being compared against")
                    .addExampleObject(new AmountItemCondition(NumberComparator.GREATER_THAN, new StaticValue(1)));
        }
    }

}