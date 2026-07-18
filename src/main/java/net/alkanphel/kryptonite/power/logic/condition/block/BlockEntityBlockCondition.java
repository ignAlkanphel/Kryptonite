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

public record BlockEntityBlockCondition() implements BlockCondition {

    public static final MapCodec<BlockEntityBlockCondition> CODEC = MapCodec.unit(BlockEntityBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, BlockEntityBlockCondition> STREAM_CODEC = StreamCodec.unit(new BlockEntityBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockEntity().isPresent();
    }

    @Override
    public BlockConditionSerializer<BlockEntityBlockCondition> getSerializer() {
        return BlockConditionSerializers.BLOCK_ENTITY.get();
    }

    public static class Serializer extends BlockConditionSerializer<BlockEntityBlockCondition> {

        @Override
        public MapCodec<BlockEntityBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, BlockEntityBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Block Entity")
                    .setDescription("Checks if the block is a block entity.")
                    .addExampleObject(new BlockEntityBlockCondition());
        }
    }

}