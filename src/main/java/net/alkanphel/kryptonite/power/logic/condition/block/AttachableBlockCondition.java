package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record AttachableBlockCondition() implements BlockCondition {

    public static final MapCodec<AttachableBlockCondition> CODEC = MapCodec.unit(AttachableBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, AttachableBlockCondition> STREAM_CODEC = StreamCodec.unit(new AttachableBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        Level level = context.level();
        BlockPos pos = context.pos();

        for (Direction direction : Direction.values()) {
            BlockPos offsetPos = pos.relative(direction);

            if (level.hasChunkAt(offsetPos) && level.getBlockState(offsetPos).isFaceSturdy(level, pos, direction.getOpposite())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public BlockConditionSerializer<AttachableBlockCondition> getSerializer() {
        return BlockConditionSerializers.ATTACHABLE.get();
    }

    public static class Serializer extends BlockConditionSerializer<AttachableBlockCondition> {

        @Override
        public MapCodec<AttachableBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, AttachableBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Attachable")
                    .setDescription("Checks if the block is in a place where a supported block can be attached (e.g. checks whether any of the adjacent blocks' sides towards this block position are solid).")
                    .addExampleObject(new AttachableBlockCondition());
        }
    }

}