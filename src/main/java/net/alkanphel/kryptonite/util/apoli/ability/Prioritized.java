package net.alkanphel.kryptonite.util.apoli.ability;

import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;

import java.util.List;
import java.util.function.Predicate;

public interface Prioritized {

    int getPriority();

    static <T extends Prioritized> List<T> collect(LivingEntity entity, Class<T> type) {
        return collect(entity, type, t -> true);
    }

    static <T extends Prioritized> List<T> collect(LivingEntity entity, Class<T> type, Predicate<T> filter) {
        return AbilityUtil.getEnabledInstances(entity).stream()
                .map(AbilityInstance::getAbility)
                .filter(type::isInstance)
                .map(type::cast)
                .filter(filter)
                .sorted((a, b) -> Integer.compare(b.getPriority(), a.getPriority()))
                .toList();
    }

}