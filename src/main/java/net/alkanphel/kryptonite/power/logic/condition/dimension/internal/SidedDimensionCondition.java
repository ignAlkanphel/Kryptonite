package net.alkanphel.kryptonite.power.logic.condition.dimension.internal;

import net.alkanphel.kryptonite.power.logic.DimensionLogicEnvironment;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.resources.Identifier;

public record SidedDimensionCondition(DimensionCondition condition, DimensionLogicEnvironment environment, Identifier typeId) implements DimensionCondition {

    public static DimensionCondition wrap(DimensionCondition condition) {
        var serializer = condition.getSerializer();
        var environment = serializer.getEnvironment();

        if (environment == DimensionLogicEnvironment.BOTH) {
            return condition;
        }

        return new SidedDimensionCondition(condition, environment, KryptoniteRegistries.DIMENSION_CONDITION_SERIALIZER.getKey(serializer));
    }

    public static DimensionCondition unwrap(DimensionCondition condition) {
        return condition instanceof SidedDimensionCondition sided ? sided.condition : condition;
    }

    @Override
    public boolean test(DimensionConditionContext context) {
        if (!environment.matches(context)) {
            environment.warnMismatch(typeId);
        }

        return condition.test(context);
    }

    @Override
    public DimensionConditionSerializer<?> getSerializer() {
        return condition.getSerializer();
    }

}