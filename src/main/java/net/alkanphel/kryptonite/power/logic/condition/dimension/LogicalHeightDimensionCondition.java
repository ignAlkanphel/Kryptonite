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

public record LogicalHeightDimensionCondition(NumberComparator comparator, int compareTo) implements DimensionCondition {

    public static final MapCodec<LogicalHeightDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            NumberComparator.CODEC.fieldOf("comparator").forGetter(LogicalHeightDimensionCondition::comparator),
            Codec.INT.fieldOf("compare_to").forGetter(LogicalHeightDimensionCondition::compareTo)
    ).apply(instance, LogicalHeightDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, LogicalHeightDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            NumberComparator.STREAM_CODEC, LogicalHeightDimensionCondition::comparator,
            ByteBufCodecs.INT, LogicalHeightDimensionCondition::compareTo,
            LogicalHeightDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return comparator.compare(context.dimensionType().logicalHeight(), compareTo);
    }

    @Override
    public DimensionConditionSerializer<LogicalHeightDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.LOGICAL_HEIGHT.get();
    }

    public static class Serializer extends DimensionConditionSerializer<LogicalHeightDimensionCondition> {

        @Override
        public MapCodec<LogicalHeightDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, LogicalHeightDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Height")
                    .setDescription("Checks the logical height of the current dimension.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used")
                    .addOptional("compare_to", SettingType.combined(SettingType.intRange(0, DimensionType.Y_SIZE)), "Value that is being compared against")
                    .addExampleObject(new LogicalHeightDimensionCondition(NumberComparator.EQUALS, 128));
        }
    }

}