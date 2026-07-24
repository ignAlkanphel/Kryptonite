package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Arrays;

public class IsInSnowCondition implements Condition {

    public static final IsInSnowCondition INSTANCE = new IsInSnowCondition();

    public static final MapCodec<IsInSnowCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsInSnowCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        return inSnow(entity.level(), BlockPos.containing(MiscUtil.getPoseDependentEyePos(entity)), entity.blockPosition());
    }

    public static boolean inSnow(Level level, BlockPos... blockPositions) {
        return Arrays.stream(blockPositions).anyMatch(blockPos -> level.getBiome(blockPos).value().getPrecipitationAt(blockPos, level.getSeaLevel()) == Biome.Precipitation.SNOW && isRainingAndExposed(level, blockPos));
    }

    private static boolean isRainingAndExposed(Level level, BlockPos blockPos) {
        return level.isRaining()
                && level.canSeeSky(blockPos)
                && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() < blockPos.getY();
    }

    @Override
    public ConditionSerializer<IsInSnowCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_IN_SNOW.get();
    }

    public static class Serializer extends ConditionSerializer<IsInSnowCondition> {

        @Override
        public MapCodec<IsInSnowCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsInSnowCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is in Snow")
                    .setDescription("Checks if it's snowing at the entity's position.")
                    .addExampleObject(INSTANCE);
        }
    }

}