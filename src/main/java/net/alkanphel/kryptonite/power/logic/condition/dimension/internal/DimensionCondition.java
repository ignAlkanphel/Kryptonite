package net.alkanphel.kryptonite.power.logic.condition.dimension.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.condition.dimension.meta.AndDimensionCondition;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.threetag.palladium.power.ability.AbilityInstance;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface DimensionCondition {

    Codec<DimensionCondition> DIRECT_CODEC = KryptoniteRegistries.DIMENSION_CONDITION_SERIALIZER.byNameCodec().dispatch(DimensionCondition::getSerializer, DimensionConditionSerializer::codec);
    Codec<List<DimensionCondition>> LIST_CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC);
    Codec<DimensionCondition> CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC).xmap(AndDimensionCondition::new, condition -> condition instanceof AndDimensionCondition(List<DimensionCondition> conditions) ? conditions : Collections.singletonList(condition));

    boolean test(DimensionConditionContext context);

    default boolean test(DimensionType dimensionType, @Nullable Level level, @Nullable Entity entity) {
        return this.test(new DimensionConditionContext(dimensionType, level, entity));
    }

    DimensionConditionSerializer<?> getSerializer();

    // leftovers/misc. below

    default void init(LivingEntity entity, AbilityInstance<?> abilityInstance) {}

    default List<String> getDependentAbilities() {
        return Collections.emptyList();
    }

    static boolean checkConditions(Collection<DimensionCondition> conditions, DimensionConditionContext context) {
        for (DimensionCondition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }

        return true;
    }

}