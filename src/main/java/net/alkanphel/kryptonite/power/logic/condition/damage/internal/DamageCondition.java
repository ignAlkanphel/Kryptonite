package net.alkanphel.kryptonite.power.logic.condition.damage.internal;

import com.mojang.serialization.Codec;
import net.alkanphel.kryptonite.power.logic.condition.damage.meta.AndDamageCondition;
import net.alkanphel.kryptonite.power.logic.context.DamageConditionContext;
import net.alkanphel.kryptonite.registry.KryptoniteRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.damagesource.DamageSource;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public interface DamageCondition {

    Codec<DamageCondition> DIRECT_CODEC = KryptoniteRegistries.DAMAGE_CONDITION_SERIALIZER.byNameCodec().dispatch(DamageCondition::getSerializer, DamageConditionSerializer::codec);
    Codec<List<DamageCondition>> LIST_CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC);
    Codec<DamageCondition> CODEC = ExtraCodecs.compactListCodec(DIRECT_CODEC).xmap(AndDamageCondition::new, condition -> condition instanceof AndDamageCondition(List<DamageCondition> conditions) ? conditions : Collections.singletonList(condition));

    boolean test(DamageConditionContext context);

    default boolean test(DamageSource source, float amount) {
        return test(new DamageConditionContext(source, amount));
    }

    static boolean checkConditions(Collection<DamageCondition> conditions, DamageSource source, float amount) {
        return checkConditions(conditions, new DamageConditionContext(source, amount));
    }

    static boolean checkConditions(Collection<DamageCondition> conditions, DamageConditionContext context) {
        for (DamageCondition condition : conditions) {
            if (!condition.test(context)) {
                return false;
            }
        }

        return true;
    }

    DamageConditionSerializer<?> getSerializer();

}