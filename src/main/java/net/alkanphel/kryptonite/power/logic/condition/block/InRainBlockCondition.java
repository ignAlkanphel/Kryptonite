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

public record InRainBlockCondition() implements BlockCondition {

    public static final MapCodec<InRainBlockCondition> CODEC = MapCodec.unit(InRainBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, InRainBlockCondition> STREAM_CODEC = StreamCodec.unit(new InRainBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.level().isRainingAt(context.pos().above());
    }

    @Override
    public BlockConditionSerializer<InRainBlockCondition> getSerializer() {
        return BlockConditionSerializers.IN_RAIN.get();
    }

    public static class Serializer extends BlockConditionSerializer<InRainBlockCondition> {

        @Override
        public MapCodec<InRainBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, InRainBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("In Rain")
                    .setDescription("Checks if it's raining above the block.")
                    .addExampleObject(new InRainBlockCondition());
        }
    }

}