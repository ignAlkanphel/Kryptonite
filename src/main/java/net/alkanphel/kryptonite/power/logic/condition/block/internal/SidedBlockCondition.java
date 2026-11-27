package net.alkanphel.kryptonite.power.logic.condition.block.internal;

import net.alkanphel.kryptonite.power.logic.BlockLogicEnvironment;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.resources.Identifier;

public record SidedBlockCondition(BlockCondition condition, BlockLogicEnvironment environment, Identifier typeId) implements BlockCondition {

    public static BlockCondition wrap(BlockCondition condition) {
        var serializer = condition.getSerializer();
        var environment = serializer.getEnvironment();

        if (environment == BlockLogicEnvironment.BOTH) {
            return condition;
        }

        return new SidedBlockCondition(condition, environment, KryptoniteRegistries.BLOCK_CONDITION_SERIALIZER.getKey(serializer));
    }

    public static BlockCondition unwrap(BlockCondition condition) {
        return condition instanceof SidedBlockCondition sided ? sided.condition : condition;
    }

    @Override
    public boolean test(BlockConditionContext context) {
        if (!this.environment.matches(context)) {
            this.environment.warnMismatch(this.typeId);
        }

        return this.condition.test(context);
    }

    @Override
    public BlockConditionSerializer<?> getSerializer() {
        return this.condition.getSerializer();
    }

}