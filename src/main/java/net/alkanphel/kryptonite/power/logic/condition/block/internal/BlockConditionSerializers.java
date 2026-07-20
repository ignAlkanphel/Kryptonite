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

    public static final DeferredHolder<BlockConditionSerializer<?>, AdjacentBlockCondition.Serializer> ADJACENT = BLOCK_CONDITION_SERIALIZERS.register("adjacent", AdjacentBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, AttachableBlockCondition.Serializer> ATTACHABLE = BLOCK_CONDITION_SERIALIZERS.register("attachable", AttachableBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, BlockBlockCondition.Serializer> BLOCK = BLOCK_CONDITION_SERIALIZERS.register("block", BlockBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, BlockEntityBlockCondition.Serializer> BLOCK_ENTITY = BLOCK_CONDITION_SERIALIZERS.register("block_entity", BlockEntityBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, BlockStateBlockCondition.Serializer> BLOCK_STATE = BLOCK_CONDITION_SERIALIZERS.register("block_state", BlockStateBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, CanSeeSkyBlockCondition.Serializer> CAN_SEE_SKY = BLOCK_CONDITION_SERIALIZERS.register("can_see_sky", CanSeeSkyBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, CommandResultBlockCondition.Serializer> COMMAND_RESULT = BLOCK_CONDITION_SERIALIZERS.register("command_result", CommandResultBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, DestroySpeedBlockCondition.Serializer> DESTROY_SPEED = BLOCK_CONDITION_SERIALIZERS.register("destroy_speed", DestroySpeedBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, DistanceFromCoordinatesBlockCondition.Serializer> DISTANCE_FROM_COORDINATES = BLOCK_CONDITION_SERIALIZERS.register("distance_from_coordinates", DistanceFromCoordinatesBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, ExplosionResistanceBlockCondition.Serializer> EXPLOSION_RESISTANCE = BLOCK_CONDITION_SERIALIZERS.register("explosion_resistance", ExplosionResistanceBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, FluidTypeBlockCondition.Serializer> FLUID_TYPE = BLOCK_CONDITION_SERIALIZERS.register("fluid_type", FluidTypeBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, FrictionBlockCondition.Serializer> FRICTION = BLOCK_CONDITION_SERIALIZERS.register("friction", FrictionBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, HeightBlockCondition.Serializer> HEIGHT = BLOCK_CONDITION_SERIALIZERS.register("height", HeightBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, IsBlockBlockCondition.Serializer> IS_BLOCK = BLOCK_CONDITION_SERIALIZERS.register("is_block", IsBlockBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, LightBlockingBlockCondition.Serializer> LIGHT_BLOCKING = BLOCK_CONDITION_SERIALIZERS.register("light_blocking", LightBlockingBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, LightLevelBlockCondition.Serializer> LIGHT_LEVEL = BLOCK_CONDITION_SERIALIZERS.register("light_level", LightLevelBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, MovementBlockingBlockCondition.Serializer> MOVEMENT_BLOCKING = BLOCK_CONDITION_SERIALIZERS.register("movement_blocking", MovementBlockingBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, NbtBlockCondition.Serializer> NBT = BLOCK_CONDITION_SERIALIZERS.register("nbt", NbtBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, OffsetBlockCondition.Serializer> OFFSET = BLOCK_CONDITION_SERIALIZERS.register("offset", OffsetBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, ReplacableBlockCondition.Serializer> REPLACEABLE = BLOCK_CONDITION_SERIALIZERS.register("replaceable", ReplacableBlockCondition.Serializer::new);
    public static final DeferredHolder<BlockConditionSerializer<?>, WaterLoggableBlockCondition.Serializer> WATER_LOGGABLE = BLOCK_CONDITION_SERIALIZERS.register("water_loggable", WaterLoggableBlockCondition.Serializer::new);

}