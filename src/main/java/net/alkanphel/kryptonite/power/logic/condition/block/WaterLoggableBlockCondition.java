package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record WaterLoggableBlockCondition() implements BlockCondition {

    public static final MapCodec<WaterLoggableBlockCondition> CODEC = MapCodec.unit(WaterLoggableBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, WaterLoggableBlockCondition> STREAM_CODEC = StreamCodec.unit(new WaterLoggableBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.blockState().getBlock() instanceof SimpleWaterloggedBlock;
    }

    @Override
    public BlockConditionSerializer<WaterLoggableBlockCondition> getSerializer() {
        return BlockConditionSerializers.WATER_LOGGABLE.get();
    }

    public static class Serializer extends BlockConditionSerializer<WaterLoggableBlockCondition> {

        @Override
        public MapCodec<WaterLoggableBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, WaterLoggableBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Water Loggable")
                    .setDescription("Checks if the block is waterloggable, meaning that there can be fluid in the same block space (e.g. stairs).")
                    .addExampleObject(new WaterLoggableBlockCondition());
        }
    }

}