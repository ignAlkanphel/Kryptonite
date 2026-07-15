package net.alkanphel.kryptonite.power.logic.condition.block.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.condition.block.meta.AndBlockCondition;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.alkanphel.kryptonite.util.apoli.SavedBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface BlockCondition {

    Codec<BlockCondition> DIRECT_CODEC = KryptoniteRegistries.BLOCK_CONDITION_SERIALIZER.byNameCodec().dispatch(BlockCondition::getSerializer, BlockConditionSerializer::codec);
    Codec<List<BlockCondition>> LIST_CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC);
    Codec<BlockCondition> CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC).xmap(AndBlockCondition::new, condition -> condition instanceof AndBlockCondition(List<BlockCondition> conditions) ? conditions : Collections.singletonList(condition));

    boolean test(BlockConditionContext context);

    default boolean test(SavedBlockPosition savedBlock) {
        return test(new BlockConditionContext(savedBlock));
    }

    default boolean test(Level level, BlockPos pos) {
        return test(new BlockConditionContext(level, pos));
    }

    static boolean checkConditions(Collection<BlockCondition> conditions, SavedBlockPosition savedBlock) {
        return checkConditions(conditions, new BlockConditionContext(savedBlock));
    }

    static boolean checkConditions(Collection<BlockCondition> conditions, Level level, BlockPos pos) {
        return checkConditions(conditions, new BlockConditionContext(level, pos));
    }

    static boolean checkConditions(Collection<BlockCondition> conditions, BlockConditionContext context) {
        for (BlockCondition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }

        return true;
    }

    BlockConditionSerializer<?> getSerializer();

}