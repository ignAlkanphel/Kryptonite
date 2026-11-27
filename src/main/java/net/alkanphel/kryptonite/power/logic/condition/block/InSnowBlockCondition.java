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

public record InSnowBlockCondition() implements BlockCondition {

    public static final MapCodec<InSnowBlockCondition> CODEC = MapCodec.unit(InSnowBlockCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, InSnowBlockCondition> STREAM_CODEC = StreamCodec.unit(new InSnowBlockCondition());

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
    public BlockConditionSerializer<InSnowBlockCondition> getSerializer() {
        return BlockConditionSerializers.IN_SNOW.get();
    }

    public static class Serializer extends BlockConditionSerializer<InSnowBlockCondition> {

        @Override
        public MapCodec<InSnowBlockCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BlockCondition, InSnowBlockCondition> builder, HolderLookup.Provider provider) {
            builder.setName("In Snow")
                    .setDescription("Checks if it's snowing above the block.")
                    .addExampleObject(new InSnowBlockCondition());
        }
    }

}