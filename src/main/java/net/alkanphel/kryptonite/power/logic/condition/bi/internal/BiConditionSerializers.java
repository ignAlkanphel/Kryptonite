package net.alkanphel.kryptonite.power.logic.condition.bi.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BiConditionSerializers {
    public static final DeferredRegister<BiConditionSerializer<?>> BI_CONDITION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BI_CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<BiConditionSerializer<?>, AndBiCondition.Serializer> AND = BI_CONDITION_SERIALIZERS.register("and", AndBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, OrBiCondition.Serializer> OR = BI_CONDITION_SERIALIZERS.register("or", OrBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, NotBiCondition.Serializer> NOT = BI_CONDITION_SERIALIZERS.register("not", NotBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, FalseBiCondition.Serializer> FALSE = BI_CONDITION_SERIALIZERS.register("false", FalseBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, TrueBiCondition.Serializer> TRUE = BI_CONDITION_SERIALIZERS.register("true", TrueBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, ActorConditionBiCondition.Serializer> ACTOR_CONDITION = BI_CONDITION_SERIALIZERS.register("actor_condition", ActorConditionBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, TargetConditionBiCondition.Serializer> TARGET_CONDITION = BI_CONDITION_SERIALIZERS.register("target_condition", TargetConditionBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, InvertBiCondition.Serializer> INVERT = BI_CONDITION_SERIALIZERS.register("invert", InvertBiCondition.Serializer::new);

}