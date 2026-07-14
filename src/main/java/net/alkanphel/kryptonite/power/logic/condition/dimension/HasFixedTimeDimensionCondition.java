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

public record HasFixedTimeDimensionCondition() implements DimensionCondition {

    public static final MapCodec<HasFixedTimeDimensionCondition> CODEC = MapCodec.unit(HasFixedTimeDimensionCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, HasFixedTimeDimensionCondition> STREAM_CODEC = StreamCodec.unit(new HasFixedTimeDimensionCondition());

    @Override
    public boolean test(DimensionConditionContext context) {
        return context.dimensionType().hasFixedTime();
    }

    @Override
    public DimensionConditionSerializer<HasFixedTimeDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.HAS_FIXED_TIME.get();
    }

    public static class Serializer extends DimensionConditionSerializer<HasFixedTimeDimensionCondition> {

        @Override
        public MapCodec<HasFixedTimeDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, HasFixedTimeDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Has Fixed Time")
                    .setDescription("Checks if the current dimension has a fixed time.")
                    .addExampleObject(new HasFixedTimeDimensionCondition());
        }
    }

}