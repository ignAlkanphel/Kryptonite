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

public record LightBlockingBlockCondition() implements BlockCondition {

    public static final MapCodec<LightBlockingBlockCondition> CODEC = MapCodec.unit(LightBlockingBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, LightBlockingBlockCondition> STREAM_CODEC = StreamCodec.unit(new LightBlockingBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockState().isSolidRender();
    }

    @Override
    public BlockConditionSerializer<LightBlockingBlockCondition> getSerializer() {
        return BlockConditionSerializers.LIGHT_BLOCKING.get();
    }

    public static class Serializer extends BlockConditionSerializer<LightBlockingBlockCondition> {

        @Override
        public MapCodec<LightBlockingBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, LightBlockingBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Light Blocking")
                    .setDescription("Checks if the block is marked in code as blocking light. As an example: glass would fail this check.")
                    .addExampleObject(new LightBlockingBlockCondition());
        }
    }

}