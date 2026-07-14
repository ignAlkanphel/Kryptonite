package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.clock.WorldClock;
import net.minecraft.world.clock.WorldClocks;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record DefaultClockDimensionCondition(Holder<WorldClock> defaultClock) implements DimensionCondition {

    public static final MapCodec<DefaultClockDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            WorldClock.CODEC.fieldOf("default_clock").forGetter(DefaultClockDimensionCondition::defaultClock)
    ).apply(instance, DefaultClockDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DefaultClockDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            WorldClock.STREAM_CODEC, DefaultClockDimensionCondition::defaultClock,
            DefaultClockDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return context.dimensionType().defaultClock().isPresent() && context.dimensionType().defaultClock().get().equals(defaultClock);
    }

    @Override
    public DimensionConditionSerializer<DefaultClockDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.DEFAULT_CLOCK.get();
    }

    public static class Serializer extends DimensionConditionSerializer<DefaultClockDimensionCondition> {

        @Override
        public MapCodec<DefaultClockDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, DefaultClockDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Default Clock")
                    .setDescription("Checks the default world clock of the current dimension.")
                    .add("default_clock", TYPE_IDENTIFIER, "Checks if the default clock of the dimension matches the specified id.")
                    .addExampleObject(new DefaultClockDimensionCondition(provider.lookupOrThrow(Registries.WORLD_CLOCK).getOrThrow(WorldClocks.OVERWORLD)));
        }
    }

}