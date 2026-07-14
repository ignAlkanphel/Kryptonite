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
import net.threetag.palladium.util.NumberComparator;

public record AmbientLightDimensionCondition(NumberComparator comparator, float compareTo) implements DimensionCondition {

    public static final MapCodec<AmbientLightDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(AmbientLightDimensionCondition::comparator),
            Codec.FLOAT.fieldOf("compare_to").forGetter(AmbientLightDimensionCondition::compareTo)
    ).apply(instance, AmbientLightDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AmbientLightDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, AmbientLightDimensionCondition::comparator, ByteBufCodecs.FLOAT, AmbientLightDimensionCondition::compareTo,
            AmbientLightDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return comparator.compare(context.dimensionType().ambientLight(), compareTo);
    }

    @Override
    public DimensionConditionSerializer<AmbientLightDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.AMBIENT_LIGHT.get();
    }

    public static class Serializer extends DimensionConditionSerializer<AmbientLightDimensionCondition> {

        @Override
        public MapCodec<AmbientLightDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, AmbientLightDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Ambient Light")
                    .setDescription("Checks the ambient light level of the current dimension.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used")
                    .addOptional("compare_to", TYPE_FLOAT, "Value that is being compared against")
                    .addExampleObject(new AmbientLightDimensionCondition(NumberComparator.GREATER_OR_EQUAL, 0.25f));
        }
    }

}