package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record DistanceBiCondition(NumberComparator comparator, Value compareTo) implements BiCondition {

    public static final MapCodec<DistanceBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(DistanceBiCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(DistanceBiCondition::compareTo)
    ).apply(instance, DistanceBiCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DistanceBiCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, DistanceBiCondition::comparator,
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Value.CODEC), DistanceBiCondition::compareTo,
            DistanceBiCondition::new
    );

    @Override
    public boolean test(BiConditionContext context) {
        if (context.actor() == null || context.target() == null) return false;

        DataContext dataContext = DataContext.forEntity(context.actor()).with(DataContextKeys.ENTITY, context.target());
        double distance = compareTo.getAsDouble(dataContext);

        return comparator.compare(context.actor().distanceToSqr(context.target()), distance * distance);
    }

    @Override
    public BiConditionSerializer<DistanceBiCondition> getSerializer() {
        return BiConditionSerializers.DISTANCE.get();
    }

    public static class Serializer extends BiConditionSerializer<DistanceBiCondition> {

        @Override
        public MapCodec<DistanceBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, DistanceBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Distance")
                    .setDescription("Compares the distance between the actor & target entities.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator.")
                    .add("compare_to", TYPE_VALUE, "The distance value to compare against.")
                    .addExampleObject(new DistanceBiCondition(NumberComparator.LESS_OR_EQUAL, new StaticValue(10.0D)));
        }
    }

}