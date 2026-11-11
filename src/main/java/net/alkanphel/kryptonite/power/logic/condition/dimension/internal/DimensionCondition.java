package net.alkanphel.kryptonite.power.logic.condition.dimension.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.condition.dimension.meta.AndDimensionCondition;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface DimensionCondition {

    Codec<DimensionCondition> DIRECT_CODEC = DimensionConditionSerializers.TYPE_CODEC.<DimensionCondition>dispatch(DimensionCondition::getSerializer, DimensionConditionSerializer::codec).xmap(SidedDimensionCondition::wrap, SidedDimensionCondition::unwrap);
    Codec<List<DimensionCondition>> LIST_CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC);
    Codec<DimensionCondition> CODEC = LIST_CODEC.xmap(AndDimensionCondition::new, condition -> condition instanceof AndDimensionCondition(List<DimensionCondition> conditions) ? conditions : Collections.singletonList(condition));

    boolean test(DimensionConditionContext context);

    default boolean test(DimensionType dimensionType, @Nullable Level level, @Nullable Entity entity) {
        return this.test(new DimensionConditionContext(dimensionType, level, entity));
    }

    static boolean checkConditions(Collection<DimensionCondition> conditions, DimensionType dimensionType, @Nullable Level level, @Nullable Entity entity) {
        return checkConditions(conditions, new DimensionConditionContext(dimensionType, level, entity));
    }

    static boolean checkConditions(Collection<DimensionCondition> conditions, DimensionConditionContext context) {
        for (DimensionCondition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }

        return true;
    }

    DimensionConditionSerializer<?> getSerializer();

}