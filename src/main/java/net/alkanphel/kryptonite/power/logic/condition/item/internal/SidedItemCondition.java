package net.alkanphel.kryptonite.power.logic.condition.item.internal;

import net.alkanphel.kryptonite.power.logic.ItemLogicEnvironment;
import net.alkanphel.kryptonite.power.logic.context.ItemConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.resources.Identifier;

public record SidedItemCondition(ItemCondition condition, ItemLogicEnvironment environment, Identifier typeId) implements ItemCondition {

    public static ItemCondition wrap(ItemCondition condition) {
        var serializer = condition.getSerializer();
        var environment = serializer.getEnvironment();

        if (environment == ItemLogicEnvironment.BOTH) {
            return condition;
        }

        return new SidedItemCondition(condition, environment, KryptoniteRegistries.ITEM_CONDITION_SERIALIZER.getKey(serializer));
    }

    public static ItemCondition unwrap(ItemCondition condition) {
        return condition instanceof SidedItemCondition sided ? sided.condition : condition;
    }

    @Override
    public boolean test(ItemConditionContext context) {
        if (!environment.matches(context)) {
            environment.warnMismatch(typeId);
        }

        return condition.test(context);
    }

    @Override
    public ItemConditionSerializer<?> getSerializer() {
        return condition.getSerializer();
    }

}