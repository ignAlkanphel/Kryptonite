package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record HasCeilingDimensionCondition() implements DimensionCondition {

    public static final MapCodec<HasCeilingDimensionCondition> CODEC = MapCodec.unit(HasCeilingDimensionCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, HasCeilingDimensionCondition> STREAM_CODEC = StreamCodec.unit(new HasCeilingDimensionCondition());

    @Override
    public boolean test(DimensionConditionContext context) {
        return context.dimensionType().hasCeiling();
    }

    @Override
    public DimensionConditionSerializer<HasCeilingDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.HAS_CEILING.get();
    }

    public static class Serializer extends DimensionConditionSerializer<HasCeilingDimensionCondition> {

        @Override
        public MapCodec<HasCeilingDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, HasCeilingDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Has Ceiling")
                    .setDescription("Checks if the current dimension has a bedrock ceiling.")
                    .addExampleObject(new HasCeilingDimensionCondition());
        }
    }

}