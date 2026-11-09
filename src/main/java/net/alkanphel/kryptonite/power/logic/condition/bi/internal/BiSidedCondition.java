package net.alkanphel.kryptonite.power.logic.condition.bi.internal;

import net.alkanphel.kryptonite.power.logic.BiLogicEnvironment;
import net.alkanphel.kryptonite.power.logic.context.BiContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.power.ability.AbilityInstance;

import java.util.List;

public record BiSidedCondition(BiCondition condition, BiLogicEnvironment environment, Identifier typeId) implements BiCondition {

    public static BiCondition wrap(BiCondition condition) {
        var serializer = condition.getSerializer();
        var environment = serializer.getEnvironment();

        if (environment == BiLogicEnvironment.BOTH) {
            return condition;
        }

        return new BiSidedCondition(condition, environment, KryptoniteRegistries.BI_CONDITION_SERIALIZER.getKey(serializer));
    }

    public static BiCondition unwrap(BiCondition condition) {
        return condition instanceof BiSidedCondition sided ? sided.condition : condition;
    }

    @Override
    public boolean test(BiContext context) {
        if (!this.environment.matches(context)) {
            this.environment.warnMismatch(this.typeId);
        }

        return this.condition.test(context);
    }

    @Override
    public void init(Entity actor, Entity target, AbilityInstance<?> abilityInstance) {
        this.condition.init(actor, target, abilityInstance);
    }

    @Override
    public BiConditionSerializer<?> getSerializer() {
        return this.condition.getSerializer();
    }

    @Override
    public List<String> getDependentAbilities() {
        return this.condition.getDependentAbilities();
    }

}