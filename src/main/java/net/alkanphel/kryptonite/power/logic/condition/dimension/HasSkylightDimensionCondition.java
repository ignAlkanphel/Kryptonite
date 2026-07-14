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

public record HasSkylightDimensionCondition() implements DimensionCondition {

    public static final MapCodec<HasSkylightDimensionCondition> CODEC = MapCodec.unit(HasSkylightDimensionCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, HasSkylightDimensionCondition> STREAM_CODEC = StreamCodec.unit(new HasSkylightDimensionCondition());

    @Override
    public boolean test(DimensionConditionContext context) {
        return context.dimensionType().hasSkyLight();
    }

    @Override
    public DimensionConditionSerializer<HasSkylightDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.HAS_SKYLIGHT.get();
    }

    public static class Serializer extends DimensionConditionSerializer<HasSkylightDimensionCondition> {

        @Override
        public MapCodec<HasSkylightDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, HasSkylightDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Has Skylight")
                    .setDescription("Checks if the current dimension has skylight.")
                    .addExampleObject(new HasSkylightDimensionCondition());
        }
    }

}