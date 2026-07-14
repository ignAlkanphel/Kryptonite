package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record DimensionDimensionCondition(ResourceKey<Level> dimension) implements DimensionCondition {

    public static final MapCodec<DimensionDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(DimensionDimensionCondition::dimension)
    ).apply(instance, DimensionDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DimensionDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), DimensionDimensionCondition::dimension,
            DimensionDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        return context.level() != null && context.level().dimension().equals(this.dimension);
    }

    @Override
    public DimensionConditionSerializer<DimensionDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.DIMENSION.get();
    }

    public static class Serializer extends DimensionConditionSerializer<DimensionDimensionCondition> {

        @Override
        public MapCodec<DimensionDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, DimensionDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Dimension")
                    .setDescription("Checks if the current dimension matches the dimension id.")
                    .add("dimension", TYPE_IDENTIFIER, "Dimension id to check.")
                    .addExampleObject(new DimensionDimensionCondition(Level.OVERWORLD));
        }
    }

}