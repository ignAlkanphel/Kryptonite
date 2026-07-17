package net.alkanphel.kryptonite.util.apoli.ability;

import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public interface Prioritized {
    int getPriority();

    class CallInstance<T extends Ability & Prioritized> {
        private final NavigableMap<Integer, List<T>> buckets = new TreeMap<>(Comparator.reverseOrder());

        public <U extends T> void add(LivingEntity entity, Class<U> classy) {
            add(entity, classy, u -> true);
        }

        public <U extends T> void add(LivingEntity entity, Class<U> classy, @NotNull Predicate<U> filter) {
            AbilityUtil.getEnabledInstances(entity).stream().map(AbilityInstance::getAbility).filter(classy::isInstance).map(classy::cast).filter(filter).forEach(this::add);
        }

        public void add(T t) {
            buckets.computeIfAbsent(t.getPriority(), i -> new LinkedList<>()).add(t);
        }

        public List<T> getAllAbilities() {
            return buckets.values().stream().flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
        }

        public void forEachByPriority(Consumer<T> action) {
            buckets.values().forEach(list -> list.forEach(action));
        }

        public void forEachBucketUntil(Predicate<List<T>> bucketAction) {
            for (List<T> abilities : buckets.values()) {
                if (bucketAction.test(abilities)) break;
            }
        }

        public boolean isEmpty() {
            return buckets.isEmpty();
        }
    }

}