package net.alkanphel.kryptonite.power.logic.condition.damage.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.damage.*;
import net.alkanphel.kryptonite.power.logic.condition.damage.meta.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DamageConditionSerializers {

    public static final DeferredRegister<DamageConditionSerializer<?>> DAMAGE_CONDITION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.DAMAGE_CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<DamageConditionSerializer<?>, AndDamageCondition.Serializer> AND = DAMAGE_CONDITION_SERIALIZERS.register("and", AndDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, NotDamageCondition.Serializer> NOT = DAMAGE_CONDITION_SERIALIZERS.register("not", NotDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, OrDamageCondition.Serializer> OR = DAMAGE_CONDITION_SERIALIZERS.register("or", OrDamageCondition.Serializer::new);

    public static final DeferredHolder<DamageConditionSerializer<?>, AmountDamageCondition.Serializer> AMOUNT = DAMAGE_CONDITION_SERIALIZERS.register("amount", AmountDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, AttackerDamageCondition.Serializer> ATTACKER = DAMAGE_CONDITION_SERIALIZERS.register("attacker", AttackerDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, DamageTypeDamageCondition.Serializer> DAMAGE_TYPE = DAMAGE_CONDITION_SERIALIZERS.register("damage_type", DamageTypeDamageCondition.Serializer::new);
    public static final DeferredHolder<DamageConditionSerializer<?>, ProjectileDamageCondition.Serializer> PROJECTILE = DAMAGE_CONDITION_SERIALIZERS.register("projectile", ProjectileDamageCondition.Serializer::new);

}