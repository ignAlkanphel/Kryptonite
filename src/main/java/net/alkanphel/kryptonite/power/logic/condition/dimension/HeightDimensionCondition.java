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

public record HeightDimensionCondition(NumberComparator comparator, int compareTo) implements DimensionCondition {

    public static final MapCodec<HeightDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(HeightDimensionCondition::comparator),
            Codec.INT.fieldOf("compare_to").forGetter(HeightDimensionCondition::compareTo)
    ).apply(instance, HeightDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, HeightDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, HeightDimensionCondition::comparator,
            ByteBufCodecs.INT, HeightDimensionCondition::compareTo,
            HeightDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return comparator.compare(context.dimensionType().height(), compareTo);
    }

    @Override
    public DimensionConditionSerializer<HeightDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.HEIGHT.get();
    }

    public static class Serializer extends DimensionConditionSerializer<HeightDimensionCondition> {

        @Override
        public MapCodec<HeightDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, HeightDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Height")
                    .setDescription("Checks the height of the current dimension.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used")
                    .addOptional("compare_to", SettingType.combined(SettingType.intRange(16, DimensionType.Y_SIZE)), "Value that is being compared against")
                    .addExampleObject(new HeightDimensionCondition(NumberComparator.GREATER_THAN, 256));
        }
    }

}