package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record DistanceFromEntityCondition(NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<DistanceFromEntityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(DistanceFromEntityCondition::comparator),
            Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(DistanceFromEntityCondition::compareTo)
    ).apply(instance, DistanceFromEntityCondition::new));

    @Override
    public boolean test(DataContext context) {
        var source = context.getSource();
        var target = context.getTarget();

        if (source == null || target == null) return false;

        double distance = compareTo.getAsDouble(context);

        return comparator.compare(source.distanceToSqr(target), distance * distance);
    }

    @Override
    public ConditionSerializer<DistanceFromEntityCondition> getSerializer() {
        return KryptoniteConditionSerializers.DISTANCE_FROM_ENTITY.get();
    }

    public static class Serializer extends ConditionSerializer<DistanceFromEntityCondition> {

        @Override
        public MapCodec<DistanceFromEntityCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, DistanceFromEntityCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Distance From Entity")
                    .setDescription("Compares the distance between the source & target entities.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used.")
                    .add("compare_to", TYPE_VALUE, "Value that is being compared against.")
                    .addExampleObject(new DistanceFromEntityCondition(NumberComparator.LESS_OR_EQUAL, new StaticValue(10.0D)));
        }
    }

}