package net.alkanphel.kryptonite.power.logic.condition.dimension;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.CardinalLighting;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record CardinalLightDimensionCondition(CardinalLighting.Type cardinalLight) implements DimensionCondition {

    public static final MapCodec<CardinalLightDimensionCondition> CODEC = CardinalLighting.Type.CODEC.fieldOf("cardinal_light").xmap(CardinalLightDimensionCondition::new, CardinalLightDimensionCondition::cardinalLight);

    public static final StreamCodec<RegistryFriendlyByteBuf, CardinalLightDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(CardinalLighting.Type.CODEC), CardinalLightDimensionCondition::cardinalLight,
            CardinalLightDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return context.dimensionType().cardinalLightType() == cardinalLight;
    }

    @Override
    public DimensionConditionSerializer<CardinalLightDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.CARDINAL_LIGHT.get();
    }

    public static class Serializer extends DimensionConditionSerializer<CardinalLightDimensionCondition> {

        @Override
        public MapCodec<CardinalLightDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, CardinalLightDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Cardinal Light")
                    .setDescription("Checks the cardinal lighting type of the current dimension.")
                    .add("cardinal_light", KryptoniteDocumented.TYPE_CARDINAL_LIGHTING, "Cardinal lighting type to check.")
                    .addExampleObject(new CardinalLightDimensionCondition(CardinalLighting.Type.NETHER));
        }
    }

}