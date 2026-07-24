package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record IsInRainBlockCondition() implements BlockCondition {

    public static final MapCodec<IsInRainBlockCondition> CODEC = MapCodec.unit(IsInRainBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsInRainBlockCondition> STREAM_CODEC = StreamCodec.unit(new IsInRainBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.level().isRainingAt(context.pos().above());
    }

    @Override
    public BlockConditionSerializer<IsInRainBlockCondition> getSerializer() {
        return BlockConditionSerializers.IS_IN_RAIN.get();
    }

    public static class Serializer extends BlockConditionSerializer<IsInRainBlockCondition> {

        @Override
        public MapCodec<IsInRainBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, IsInRainBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is in Rain")
                    .setDescription("Checks if it's raining above a block.")
                    .addExampleObject(new IsInRainBlockCondition());
        }
    }

}