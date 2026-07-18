package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record MovementBlockingBlockCondition() implements BlockCondition {

    public static final MapCodec<MovementBlockingBlockCondition> CODEC = MapCodec.unit(MovementBlockingBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, MovementBlockingBlockCondition> STREAM_CODEC = StreamCodec.unit(new MovementBlockingBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        BlockState state = context.blockState();
        return state.blocksMotion() && !state.getCollisionShape(context.level(), context.pos()).isEmpty();
    }

    @Override
    public BlockConditionSerializer<MovementBlockingBlockCondition> getSerializer() {
        return BlockConditionSerializers.MOVEMENT_BLOCKING.get();
    }

    public static class Serializer extends BlockConditionSerializer<MovementBlockingBlockCondition> {

        @Override
        public MapCodec<MovementBlockingBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, MovementBlockingBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Movement Blocking")
                    .setDescription("Checks if the block is marked in code as blocking movement & doesn't have an empty collision shape.")
                    .addExampleObject(new MovementBlockingBlockCondition());
        }
    }

}