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

public record HasEnderDragonFightDimensionCondition() implements DimensionCondition {

    public static final MapCodec<HasEnderDragonFightDimensionCondition> CODEC = MapCodec.unit(HasEnderDragonFightDimensionCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, HasEnderDragonFightDimensionCondition> STREAM_CODEC = StreamCodec.unit(new HasEnderDragonFightDimensionCondition());

    @Override
    public boolean test(DimensionConditionContext context) {
        return context.dimensionType().hasEnderDragonFight();
    }

    @Override
    public DimensionConditionSerializer<HasEnderDragonFightDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.HAS_ENDER_DRAGON_FIGHT.get();
    }

    public static class Serializer extends DimensionConditionSerializer<HasEnderDragonFightDimensionCondition> {

        @Override
        public MapCodec<HasEnderDragonFightDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, HasEnderDragonFightDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Has Ender Dragon Fight")
                    .setDescription("Checks if the current dimension has an Ender Dragon fight.")
                    .addExampleObject(new HasEnderDragonFightDimensionCondition());
        }
    }

}