package net.alkanphel.kryptonite.power.logic.condition.bi.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.bi.*;
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
    public static final DeferredHolder<BiConditionSerializer<?>, UndirectedBiCondition.Serializer> UNDIRECTED = BI_CONDITION_SERIALIZERS.register("undirected", UndirectedBiCondition.Serializer::new);

    public static final DeferredHolder<BiConditionSerializer<?>, AttackerBiCondition.Serializer> ATTACKER = BI_CONDITION_SERIALIZERS.register("attacker", AttackerBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, AttackTargetBiCondition.Serializer> ATTACK_TARGET = BI_CONDITION_SERIALIZERS.register("attack_target", AttackTargetBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, CanSeeBiCondition.Serializer> CAN_SEE = BI_CONDITION_SERIALIZERS.register("can_see", CanSeeBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, CollisionBiCondition.Serializer> COLLISION = BI_CONDITION_SERIALIZERS.register("collision", CollisionBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, DistanceBiCondition.Serializer> DISTANCE = BI_CONDITION_SERIALIZERS.register("distance", DistanceBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, EqualBiCondition.Serializer> EQUAL = BI_CONDITION_SERIALIZERS.register("equal", EqualBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, OwnerBiCondition.Serializer> OWNER = BI_CONDITION_SERIALIZERS.register("owner", OwnerBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, ObjectiveScoreBiEntityCondition.Serializer> OBJECTIVE_SCORE = BI_CONDITION_SERIALIZERS.register("objective_score", ObjectiveScoreBiEntityCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, RelativeRotationBiCondition.Serializer> RELATIVE_ROTATION = BI_CONDITION_SERIALIZERS.register("relative_rotation", RelativeRotationBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, VehicleRidingBiCondition.Serializer> VEHICLE_RIDING = BI_CONDITION_SERIALIZERS.register("vehicle_riding", VehicleRidingBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, VehicleRidingRecursiveBiCondition.Serializer> VEHICLE_RIDING_RECURSIVE = BI_CONDITION_SERIALIZERS.register("vehicle_riding_recursive", VehicleRidingRecursiveBiCondition.Serializer::new);
    public static final DeferredHolder<BiConditionSerializer<?>, VehicleRidingRootBiCondition.Serializer> VEHICLE_RIDING_ROOT = BI_CONDITION_SERIALIZERS.register("vehicle_riding_root", VehicleRidingRootBiCondition.Serializer::new);

}