package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.util.NumberComparator;

public record CoordinateScaleDimensionCondition(NumberComparator comparator, double compareTo) implements DimensionCondition {

    public static final MapCodec<CoordinateScaleDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(CoordinateScaleDimensionCondition::comparator),
            Codec.DOUBLE.fieldOf("compare_to").forGetter(CoordinateScaleDimensionCondition::compareTo)
    ).apply(instance, CoordinateScaleDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CoordinateScaleDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, CoordinateScaleDimensionCondition::comparator,
            ByteBufCodecs.DOUBLE, CoordinateScaleDimensionCondition::compareTo,
            CoordinateScaleDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return comparator.compare(context.dimensionType().coordinateScale(), compareTo);
    }

    @Override
    public DimensionConditionSerializer<CoordinateScaleDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.COORDINATE_SCALE.get();
    }

    public static class Serializer extends DimensionConditionSerializer<CoordinateScaleDimensionCondition> {

        @Override
        public MapCodec<CoordinateScaleDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, CoordinateScaleDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Coordinate Scale")
                    .setDescription("Checks the coordinate scale of the current dimension.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used")
                    .addOptional("compare_to", SettingType.combined(SettingType.doubleRange(1.0E-5F, 3.0E7)), "Value that is being compared against")
                    .addExampleObject(new CoordinateScaleDimensionCondition(NumberComparator.GREATER_THAN, 1.0));
        }
    }

}