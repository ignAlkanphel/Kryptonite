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
import net.minecraft.world.level.dimension.DimensionType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.util.NumberComparator;

public record MinYDimensionCondition(NumberComparator comparator, int compareTo) implements DimensionCondition {

    public static final MapCodec<MinYDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(MinYDimensionCondition::comparator),
            Codec.INT.fieldOf("compare_to").forGetter(MinYDimensionCondition::compareTo)
    ).apply(instance, MinYDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MinYDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, MinYDimensionCondition::comparator,
            ByteBufCodecs.INT, MinYDimensionCondition::compareTo,
            MinYDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return comparator.compare(context.dimensionType().minY(), compareTo);
    }

    @Override
    public DimensionConditionSerializer<MinYDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.MIN_Y.get();
    }

    public static class Serializer extends DimensionConditionSerializer<MinYDimensionCondition> {

        @Override
        public MapCodec<MinYDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, MinYDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Min Y")
                    .setDescription("Checks the min Y of the current dimension.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used")
                    .addOptional("compare_to", SettingType.combined(SettingType.intRange(DimensionType.MIN_Y, DimensionType.MAX_Y)), "Value that is being compared against")
                    .addExampleObject(new MinYDimensionCondition(NumberComparator.LESS_OR_EQUAL, -64));
        }
    }

}