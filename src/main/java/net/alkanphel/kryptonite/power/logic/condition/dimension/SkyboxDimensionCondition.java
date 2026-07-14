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
import net.minecraft.world.level.dimension.DimensionType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record SkyboxDimensionCondition(DimensionType.Skybox skybox) implements DimensionCondition {

    public static final MapCodec<SkyboxDimensionCondition> CODEC = DimensionType.Skybox.CODEC.fieldOf("skybox").xmap(SkyboxDimensionCondition::new, SkyboxDimensionCondition::skybox);

    public static final StreamCodec<RegistryFriendlyByteBuf, SkyboxDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(DimensionType.Skybox.CODEC), SkyboxDimensionCondition::skybox,
            SkyboxDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        return context.dimensionType().skybox() == skybox;
    }

    @Override
    public DimensionConditionSerializer<SkyboxDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.SKYBOX.get();
    }

    public static class Serializer extends DimensionConditionSerializer<SkyboxDimensionCondition> {

        @Override
        public MapCodec<SkyboxDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, SkyboxDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Skybox")
                    .setDescription("Checks the skybox type of the current dimension.")
                    .add("skybox", KryptoniteDocumented.TYPE_SKYBOX, "Skybox type to check.")
                    .addExampleObject(new SkyboxDimensionCondition(DimensionType.Skybox.END));
        }
    }

}