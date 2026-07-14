package net.alkanphel.kryptonite.power.logic.condition.dimension.internal;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.registry.KryptoniteRegistryKeys;
import net.alkanphel.kryptonite.power.logic.condition.dimension.*;
import net.alkanphel.kryptonite.power.logic.condition.dimension.meta.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class DimensionConditionSerializers {

    public static final DeferredRegister<DimensionConditionSerializer<?>> DIMENSION_CONDITION_SERIALIZERS = DeferredRegister.create(KryptoniteRegistryKeys.DIMENSION_CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<DimensionConditionSerializer<?>, NotDimensionCondition.Serializer> NOT = DIMENSION_CONDITION_SERIALIZERS.register("not", NotDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, OrDimensionCondition.Serializer> OR = DIMENSION_CONDITION_SERIALIZERS.register("or", OrDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, AndDimensionCondition.Serializer> AND = DIMENSION_CONDITION_SERIALIZERS.register("and", AndDimensionCondition.Serializer::new);

    public static final DeferredHolder<DimensionConditionSerializer<?>, AmbientLightDimensionCondition.Serializer> AMBIENT_LIGHT = DIMENSION_CONDITION_SERIALIZERS.register("ambient_light", AmbientLightDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, AttributesDimensionCondition.Serializer> ATTRIBUTES = DIMENSION_CONDITION_SERIALIZERS.register("attributes", AttributesDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, CardinalLightDimensionCondition.Serializer> CARDINAL_LIGHT = DIMENSION_CONDITION_SERIALIZERS.register("cardinal_light", CardinalLightDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, CoordinateScaleDimensionCondition.Serializer> COORDINATE_SCALE = DIMENSION_CONDITION_SERIALIZERS.register("coordinate_scale", CoordinateScaleDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, DimensionDimensionCondition.Serializer> DIMENSION = DIMENSION_CONDITION_SERIALIZERS.register("dimension", DimensionDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, DefaultClockDimensionCondition.Serializer> DEFAULT_CLOCK = DIMENSION_CONDITION_SERIALIZERS.register("default_clock", DefaultClockDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, HasCeilingDimensionCondition.Serializer> HAS_CEILING = DIMENSION_CONDITION_SERIALIZERS.register("has_ceiling", HasCeilingDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, HasEnderDragonFightDimensionCondition.Serializer> HAS_ENDER_DRAGON_FIGHT = DIMENSION_CONDITION_SERIALIZERS.register("has_ender_dragon_fight", HasEnderDragonFightDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, HasFixedTimeDimensionCondition.Serializer> HAS_FIXED_TIME = DIMENSION_CONDITION_SERIALIZERS.register("has_fixed_time", HasFixedTimeDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, HasSkylightDimensionCondition.Serializer> HAS_SKYLIGHT = DIMENSION_CONDITION_SERIALIZERS.register("has_skylight", HasSkylightDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, HeightDimensionCondition.Serializer> HEIGHT = DIMENSION_CONDITION_SERIALIZERS.register("height", HeightDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, InfiniburnDimensionCondition.Serializer> INFINIBURN = DIMENSION_CONDITION_SERIALIZERS.register("infiniburn", InfiniburnDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, LogicalHeightDimensionCondition.Serializer> LOGICAL_HEIGHT = DIMENSION_CONDITION_SERIALIZERS.register("logical_height", LogicalHeightDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, MinYDimensionCondition.Serializer> MIN_Y = DIMENSION_CONDITION_SERIALIZERS.register("min_y", MinYDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, MonsterSettingsDimensionCondition.Serializer> MONSTER_SETTINGS = DIMENSION_CONDITION_SERIALIZERS.register("monster_settings", MonsterSettingsDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, SkyboxDimensionCondition.Serializer> SKYBOX = DIMENSION_CONDITION_SERIALIZERS.register("skybox", SkyboxDimensionCondition.Serializer::new);
    public static final DeferredHolder<DimensionConditionSerializer<?>, TimelinesDimensionCondition.Serializer> TIMELINES = DIMENSION_CONDITION_SERIALIZERS.register("timelines", TimelinesDimensionCondition.Serializer::new);

}