package net.alkanphel.kryptonite.power.logic.condition.damage;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DamageConditionContext;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.util.NumberComparator;

public record AmountDamageCondition(NumberComparator comparator, float compareTo) implements DamageCondition {

    public static final MapCodec<AmountDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(AmountDamageCondition::comparator),
            Codec.FLOAT.fieldOf("compare_to").forGetter(AmountDamageCondition::compareTo)
    ).apply(instance, AmountDamageCondition::new));

    @Override
    public boolean test(DamageConditionContext context) {
        return comparator.compare(context.amount(), compareTo);
    }

    @Override
    public DamageConditionSerializer<AmountDamageCondition> getSerializer() {
        return DamageConditionSerializers.AMOUNT.get();
    }

    public static class Serializer extends DamageConditionSerializer<AmountDamageCondition> {

        @Override
        public MapCodec<AmountDamageCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DamageCondition, AmountDamageCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Amount")
                    .setDescription("Checks whether the damage is of a specified amount.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used")
                    .add("compare_to", TYPE_FLOAT, "Value that is being compared against")
                    .addExampleObject(new AmountDamageCondition(NumberComparator.GREATER_OR_EQUAL, 5.0F));
        }
    }

}