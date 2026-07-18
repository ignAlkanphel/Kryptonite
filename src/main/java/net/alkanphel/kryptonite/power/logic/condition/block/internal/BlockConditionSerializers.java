package net.alkanphel.kryptonite.power.logic.condition.block.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.block.*;
import net.alkanphel.kryptonite.power.logic.condition.block.meta.*;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockConditionSerializers {

    public static final DeferredRegister<BlockConditionSerializer<?>> BLOCK_CONDITION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.BLOCK_CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<BlockConditionSerializer<?>, AndBlockCondition.Serializer> AND = BLOCK_CONDITION_SERIALIZERS.register("and", AndBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, OrBlockCondition.Serializer> OR = BLOCK_CONDITION_SERIALIZERS.register("or", OrBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, NotBlockCondition.Serializer> NOT = BLOCK_CONDITION_SERIALIZERS.register("not", NotBlockCondition.Serializer::new);

    public static final DeferredHolder<BlockConditionSerializer<?>, BlockBlockCondition.Serializer> BLOCK = BLOCK_CONDITION_SERIALIZERS.register("block", BlockBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, DistanceFromCoordinatesBlockCondition.Serializer> DISTANCE_FROM_COORDINATES = BLOCK_CONDITION_SERIALIZERS.register("distance_from_coordinates", DistanceFromCoordinatesBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, FrictionBlockCondition.Serializer> FRICTION = BLOCK_CONDITION_SERIALIZERS.register("friction", FrictionBlockCondition.Serializer::new);

}