package net.alkanphel.kryptonite.power.logic.condition.block;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record InThunderBlockCondition() implements BlockCondition {

    public static final MapCodec<InThunderBlockCondition> CODEC = MapCodec.unit(InThunderBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, InThunderBlockCondition> STREAM_CODEC = StreamCodec.unit(new InThunderBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return inThunderstorm(context.level(), context.pos().above());
    }

    public static boolean inThunderstorm(Level level, BlockPos pos) {
        return level.isThundering()
                && level.isRaining()
                && level.canSeeSky(pos);
    }

    @Override
    public BlockConditionSerializer<InThunderBlockCondition> getSerializer() {
        return BlockConditionSerializers.IN_THUNDER.get();
    }

    public static class Serializer extends BlockConditionSerializer<InThunderBlockCondition> {

        @Override
        public MapCodec<InThunderBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, InThunderBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("In Thunder")
                    .setDescription("Checks if it's thundering above the block.")
                    .addExampleObject(new InThunderBlockCondition());
        }
    }

}