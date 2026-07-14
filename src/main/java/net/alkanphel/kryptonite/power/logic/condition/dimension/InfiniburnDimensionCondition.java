package net.alkanphel.kryptonite.power.logic.condition.dimension;

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
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record InfiniburnDimensionCondition(TagKey<Block> infiniburn) implements DimensionCondition {

    public static final MapCodec<InfiniburnDimensionCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.hashedCodec(net.minecraft.core.registries.Registries.BLOCK).fieldOf("infiniburn").forGetter(InfiniburnDimensionCondition::infiniburn)
    ).apply(instance, InfiniburnDimensionCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, InfiniburnDimensionCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(TagKey.hashedCodec(net.minecraft.core.registries.Registries.BLOCK)), InfiniburnDimensionCondition::infiniburn,
            InfiniburnDimensionCondition::new
    );

    @Override
    public boolean test(DimensionConditionContext context) {
        if (context.level() == null) return false;
        return context.dimensionType().infiniburn().equals(infiniburn);
    }

    @Override
    public DimensionConditionSerializer<InfiniburnDimensionCondition> getSerializer() {
        return DimensionConditionSerializers.INFINIBURN.get();
    }

    public static class Serializer extends DimensionConditionSerializer<InfiniburnDimensionCondition> {

        @Override
        public MapCodec<InfiniburnDimensionCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DimensionCondition, InfiniburnDimensionCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Infiniburn")
                    .setDescription("Checks the infiniburn block tag used by the dimension.")
                    .add("infiniburn", TYPE_BLOCK_TAG, "Infiniburn block tag to use.")
                    .addExampleObject(new InfiniburnDimensionCondition(BlockTags.INFINIBURN_OVERWORLD))
                    .addExampleObject(new InfiniburnDimensionCondition(BlockTags.INFINIBURN_NETHER))
                    .addExampleObject(new InfiniburnDimensionCondition(BlockTags.INFINIBURN_END));
        }
    }

}