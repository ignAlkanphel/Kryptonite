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

public record IsInThunderBlockCondition() implements BlockCondition {

    public static final MapCodec<IsInThunderBlockCondition> CODEC = MapCodec.unit(IsInThunderBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsInThunderBlockCondition> STREAM_CODEC = StreamCodec.unit(new IsInThunderBlockCondition());

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
    public BlockConditionSerializer<IsInThunderBlockCondition> getSerializer() {
        return BlockConditionSerializers.IS_IN_THUNDER.get();
    }

    public static class Serializer extends BlockConditionSerializer<IsInThunderBlockCondition> {

        @Override
        public MapCodec<IsInThunderBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, IsInThunderBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is in Thunder")
                    .setDescription("Checks if it's thundering above a block.")
                    .addExampleObject(new IsInThunderBlockCondition());
        }
    }

}