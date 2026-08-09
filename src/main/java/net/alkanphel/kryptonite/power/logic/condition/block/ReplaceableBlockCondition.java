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

public record ReplaceableBlockCondition() implements BlockCondition {

    public static final MapCodec<ReplaceableBlockCondition> CODEC = MapCodec.unit(ReplaceableBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ReplaceableBlockCondition> STREAM_CODEC = StreamCodec.unit(new ReplaceableBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockState().canBeReplaced();
    }

    @Override
    public BlockConditionSerializer<ReplaceableBlockCondition> getSerializer() {
        return BlockConditionSerializers.REPLACEABLE.get();
    }

    public static class Serializer extends BlockConditionSerializer<ReplaceableBlockCondition> {

        @Override
        public MapCodec<ReplaceableBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, ReplaceableBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Replaceable")
                    .setDescription("Checks if the block is able to be replaced with another (e.g. short grass, water, etc.).")
                    .addExampleObject(new ReplaceableBlockCondition());
        }
    }

}