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

public record CanSeeSkyBlockCondition() implements BlockCondition {

    public static final MapCodec<CanSeeSkyBlockCondition> CODEC = MapCodec.unit(CanSeeSkyBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, CanSeeSkyBlockCondition> STREAM_CODEC = StreamCodec.unit(new CanSeeSkyBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return context.level().canSeeSky(context.pos());
    }

    @Override
    public BlockConditionSerializer<CanSeeSkyBlockCondition> getSerializer() {
        return BlockConditionSerializers.CAN_SEE_SKY.get();
    }

    public static class Serializer extends BlockConditionSerializer<CanSeeSkyBlockCondition> {

        @Override
        public MapCodec<CanSeeSkyBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, CanSeeSkyBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Can See Sky")
                    .setDescription("Checks if the block can see the sky.")
                    .addExampleObject(new CanSeeSkyBlockCondition());
        }
    }

}