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
import net.minecraft.world.level.biome.Biome;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;

public record IsInSnowBlockCondition() implements BlockCondition {

    public static final MapCodec<IsInSnowBlockCondition> CODEC = MapCodec.unit(IsInSnowBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsInSnowBlockCondition> STREAM_CODEC = StreamCodec.unit(new IsInSnowBlockCondition());

    @Override
    public boolean test(BlockConditionContext context) {
        return inSnow(context.level(), context.pos().above());
    }

    public static boolean inSnow(Level level, BlockPos pos) {
        return level.getBiome(pos).value().getPrecipitationAt(pos, level.getSeaLevel()) == Biome.Precipitation.SNOW
                && level.isRaining()
                && level.canSeeSky(pos);
    }

    @Override
    public BlockConditionSerializer<IsInSnowBlockCondition> getSerializer() {
        return BlockConditionSerializers.IS_IN_SNOW.get();
    }

    public static class Serializer extends BlockConditionSerializer<IsInSnowBlockCondition> {

        @Override
        public MapCodec<IsInSnowBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, IsInSnowBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is in Snow")
                    .setDescription("Checks if it's snowing above a block.")
                    .addExampleObject(new IsInSnowBlockCondition());
        }
    }

}