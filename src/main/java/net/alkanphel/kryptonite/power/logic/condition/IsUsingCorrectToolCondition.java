package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.mixin.common.ServerPlayerGameModeAccessor;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;

public class IsUsingCorrectToolCondition implements Condition {

    public static final IsUsingCorrectToolCondition INSTANCE = new IsUsingCorrectToolCondition();

    public static final MapCodec<IsUsingCorrectToolCondition> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, IsUsingCorrectToolCondition> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean test(DataContext context) {
        if (!(context.getEntity() instanceof Player player)) return false;

        BlockPos breakingPos;

        if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayerGameModeAccessor accessor = (ServerPlayerGameModeAccessor) serverPlayer.gameMode;
            if (!accessor.kryptonite$isDestroyingBlock()) return false;
            breakingPos = accessor.kryptonite$getDestroyPos();
        } else {
            breakingPos = Kryptonite.PROXY.getBreakingBlockPos(player);
            if (breakingPos == null) return false;
        }

        BlockState state = player.level().getBlockState(breakingPos);
        return player.hasCorrectToolForDrops(state, player.level(), breakingPos);
    }

    @Override
    public ConditionSerializer<IsUsingCorrectToolCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_USING_CORRECT_TOOL.get();
    }

    public static class Serializer extends ConditionSerializer<IsUsingCorrectToolCondition> {

        @Override
        public MapCodec<IsUsingCorrectToolCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsUsingCorrectToolCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Using Correct Tool")
                    .setDescription("Checks if the player is breaking a block using the correct tool for drops.")
                    .addExampleObject(INSTANCE);
        }
    }

}