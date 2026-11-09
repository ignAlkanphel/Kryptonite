package net.alkanphel.kryptonite.power.logic.condition.bi.internal;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.AndBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.FalseBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TrueBiCondition;
import net.alkanphel.kryptonite.power.logic.context.BiContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.power.ability.AbilityInstance;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public interface BiCondition {

    Codec<BiCondition> DIRECT_CODEC = KryptoniteRegistries.BI_CONDITION_SERIALIZER.byNameCodec().<BiCondition>dispatch(BiCondition::getSerializer, BiConditionSerializer::codec).xmap(BiSidedCondition::wrap, BiSidedCondition::unwrap);
    Codec<BiCondition> FALSE_TRUE_WRAPPED_CODEC = Codec.either(DIRECT_CODEC, Codec.BOOL).xmap(either -> either.map(Function.identity(), right -> right ? TrueBiCondition.INSTANCE : FalseBiCondition.INSTANCE), biCondition -> biCondition instanceof TrueBiCondition ? Either.right(true) : (biCondition instanceof FalseBiCondition ? Either.right(false) : Either.left(biCondition)));
    Codec<List<BiCondition>> LIST_CODEC = ExtraCodecs.compactListCodec(FALSE_TRUE_WRAPPED_CODEC);
    Codec<BiCondition> CODEC = ExtraCodecs.compactListCodec(FALSE_TRUE_WRAPPED_CODEC).xmap(AndBiCondition::new, biCondition -> biCondition instanceof AndBiCondition(List<BiCondition> biConditions) ? biConditions : Collections.singletonList(biCondition));

    boolean test(BiContext context);

    default boolean test(Entity actor, Entity target) {
        return test(new BiContext(actor, target));
    }

    static boolean checkConditions(Collection<BiCondition> conditions, Entity actor, Entity target) {
        return checkConditions(conditions, new BiContext(actor, target));
    }

    static boolean checkConditions(Collection<BiCondition> conditions, BiContext context) {
        for (BiCondition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }

        return true;
    }

    default List<String> getDependentAbilities() {
        return Collections.emptyList();
    }

    default void init(Entity actor, Entity target, AbilityInstance<?> abilityInstance) {}

    BiConditionSerializer<?> getSerializer();

}