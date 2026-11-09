package net.alkanphel.kryptonite.power.logic.condition.damage.internal;

import net.alkanphel.kryptonite.power.logic.DamageLogicEnvironment;
import net.alkanphel.kryptonite.power.logic.context.DamageConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.resources.Identifier;

public record SidedDamageCondition(DamageCondition condition, DamageLogicEnvironment environment, Identifier typeId) implements DamageCondition {

    public static DamageCondition wrap(DamageCondition condition) {
        var serializer = condition.getSerializer();
        var environment = serializer.getEnvironment();

        if (environment == DamageLogicEnvironment.BOTH) {
            return condition;
        }

        return new SidedDamageCondition(condition, environment, KryptoniteRegistries.DAMAGE_CONDITION_SERIALIZER.getKey(serializer));
    }

    public static DamageCondition unwrap(DamageCondition condition) {
        return condition instanceof SidedDamageCondition sided ? sided.condition : condition;
    }

    @Override
    public boolean test(DamageConditionContext context) {
        if (!environment.matches(context)) {
            environment.warnMismatch(typeId);
        }

        return condition.test(context);
    }

    @Override
    public DamageConditionSerializer<?> getSerializer() {
        return condition.getSerializer();
    }

}