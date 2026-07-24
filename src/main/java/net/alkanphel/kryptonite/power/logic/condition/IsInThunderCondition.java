package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Arrays;

public class IsInThunderCondition implements Condition {

    public static final IsInThunderCondition INSTANCE = new IsInThunderCondition();

    public static final MapCodec<IsInThunderCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsInThunderCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        return inThunderstorm(entity.level(), BlockPos.containing(MiscUtil.getPoseDependentEyePos(entity)), entity.blockPosition());
    }

    public static boolean inThunderstorm(Level level, BlockPos... blockPositions) {
        return Arrays.stream(blockPositions).anyMatch(blockPos -> level.isThundering() && isRainingAndExposed(level, blockPos));
    }

    private static boolean isRainingAndExposed(Level level, BlockPos blockPos) {
        return level.isRaining()
                && level.canSeeSky(blockPos)
                && level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockPos).getY() < blockPos.getY();
    }

    @Override
    public ConditionSerializer<IsInThunderCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_IN_THUNDER.get();
    }

    public static class Serializer extends ConditionSerializer<IsInThunderCondition> {

        @Override
        public MapCodec<IsInThunderCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsInThunderCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is in Thunder")
                    .setDescription("Checks if it's thundering at the entity's position.")
                    .addExampleObject(INSTANCE);
        }
    }

}