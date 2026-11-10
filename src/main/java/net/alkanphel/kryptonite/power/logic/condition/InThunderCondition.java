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

public class InThunderCondition implements Condition {

    public static final InThunderCondition INSTANCE = new InThunderCondition();

    public static final MapCodec<InThunderCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, InThunderCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

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
    public ConditionSerializer<InThunderCondition> getSerializer() {
        return KryptoniteConditionSerializers.IN_THUNDER.get();
    }

    public static class Serializer extends ConditionSerializer<InThunderCondition> {

        @Override
        public MapCodec<InThunderCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, InThunderCondition> builder, HolderLookup.Provider provider) {
            builder.setName("In Thunder")
                    .setDescription("Checks if it's thundering at the entity's position.")
                    .addExampleObject(INSTANCE);
        }
    }

}