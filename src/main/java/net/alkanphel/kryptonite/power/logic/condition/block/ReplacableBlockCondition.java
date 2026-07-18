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

public record ReplacableBlockCondition() implements BlockCondition {

    public static final MapCodec<ReplacableBlockCondition> CODEC = MapCodec.unit(ReplacableBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ReplacableBlockCondition> STREAM_CODEC = StreamCodec.unit(new ReplacableBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockState().canBeReplaced();
    }

    @Override
    public BlockConditionSerializer<ReplacableBlockCondition> getSerializer() {
        return BlockConditionSerializers.REPLACEABLE.get();
    }

    public static class Serializer extends BlockConditionSerializer<ReplacableBlockCondition> {

        @Override
        public MapCodec<ReplacableBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, ReplacableBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Replacable")
                    .setDescription("Checks if the block is a replaceable material (e.g. short grass, water, ...).")
                    .addExampleObject(new ReplacableBlockCondition());
        }
    }

}