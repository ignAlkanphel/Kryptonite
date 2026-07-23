package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.logic.condition.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteConditionSerializers {

    public static final DeferredRegister<ConditionSerializer<?>> CONDITIONS_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.CONDITION_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<ConditionSerializer<?>, AdvancementCondition.Serializer> ADVANCEMENT = CONDITIONS_SERIALIZERS.register("advancement", AdvancementCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, AttributeCondition.Serializer> ATTRIBUTE = CONDITIONS_SERIALIZERS.register("attribute", AttributeCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, BlockBreakingCondition.Serializer> BLOCK_BREAKING = CONDITIONS_SERIALIZERS.register("block_breaking", BlockBreakingCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, BlockCollisionCondition.Serializer> BLOCK_COLLISION = CONDITIONS_SERIALIZERS.register("block_collision", BlockCollisionCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, BlockCollisionHorizontalCondition.Serializer> BLOCK_COLLISION_HORIZONTAL = CONDITIONS_SERIALIZERS.register("block_collision_horizontal", BlockCollisionHorizontalCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, BlockInAnywhereCondition.Serializer> BLOCK_IN_ANYWHERE = CONDITIONS_SERIALIZERS.register("block_in_anywhere", BlockInAnywhereCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, BlockInCondition.Serializer> BLOCK_IN = CONDITIONS_SERIALIZERS.register("block_in", BlockInCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, BlockOnCondition.Serializer> BLOCK_ON = CONDITIONS_SERIALIZERS.register("block_on", BlockOnCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, CalendarCondition.Serializer> CALENDAR = CONDITIONS_SERIALIZERS.register("calendar", CalendarCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, DimensionWrapperCondition.Serializer> DIMENSION = CONDITIONS_SERIALIZERS.register("dimension", DimensionWrapperCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, DistanceFromCoordinatesCondition.Serializer> DISTANCE_FROM_COORDINATES = CONDITIONS_SERIALIZERS.register("distance_from_coordinates", DistanceFromCoordinatesCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, KeyBindCondition.Serializer> KEY_BIND = CONDITIONS_SERIALIZERS.register("key_bind", KeyBindCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, EntityInRadiusCondition.Serializer> ENTITY_IN_RADIUS = CONDITIONS_SERIALIZERS.register("entity_in_radius", EntityInRadiusCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, EquipmentCondition.Serializer> EQUIPMENT = CONDITIONS_SERIALIZERS.register("equipment", EquipmentCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, FluidHeightCondition.Serializer> FLUID_HEIGHT = CONDITIONS_SERIALIZERS.register("fluid_height", FluidHeightCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, HasEffectCondition.Serializer> HAS_EFFECT = CONDITIONS_SERIALIZERS.register("has_effect", HasEffectCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, HasRespawnCondition.Serializer> HAS_RESPAWN = CONDITIONS_SERIALIZERS.register("has_respawn", HasRespawnCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, HasUUIDCondition.Serializer> HAS_UUID = CONDITIONS_SERIALIZERS.register("has_uuid", HasUUIDCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsEffectPossibleCondition.Serializer> IS_EFFECT_POSSIBLE = CONDITIONS_SERIALIZERS.register("is_effect_possible", IsEffectPossibleCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsEntityCondition.Serializer> IS_ENTITY = CONDITIONS_SERIALIZERS.register("is_entity", IsEntityCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsGlowingCondition.Serializer> IS_GLOWING = CONDITIONS_SERIALIZERS.register("is_glowing", IsGlowingCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsPassengerCondition.Serializer> IS_PASSENGER = CONDITIONS_SERIALIZERS.register("is_passenger", IsPassengerCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsPassengerRecursiveCondition.Serializer> IS_PASSENGER_RECURSIVE = CONDITIONS_SERIALIZERS.register("is_passenger_recursive", IsPassengerRecursiveCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsRidingCondition.Serializer> IS_RIDING = CONDITIONS_SERIALIZERS.register("is_riding", IsRidingCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsRidingRecursiveCondition.Serializer> IS_RIDING_RECURSIVE = CONDITIONS_SERIALIZERS.register("is_riding_recursive", IsRidingRecursiveCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsRidingRootCondition.Serializer> IS_RIDING_ROOT = CONDITIONS_SERIALIZERS.register("is_riding_root", IsRidingRootCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsTamedCondition.Serializer> IS_TAMED = CONDITIONS_SERIALIZERS.register("is_tamed", IsTamedCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsUsingCorrectToolCondition.Serializer> IS_USING_CORRECT_TOOL = CONDITIONS_SERIALIZERS.register("is_using_correct_tool", IsUsingCorrectToolCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, IsUsingItemCondition.Serializer> IS_USING_ITEM = CONDITIONS_SERIALIZERS.register("is_using_item", IsUsingItemCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, NbtCondition.Serializer> NBT = CONDITIONS_SERIALIZERS.register("nbt", NbtCondition.Serializer::new);
    public static final DeferredHolder<ConditionSerializer<?>, PredicateCondition.Serializer> PREDICATE = CONDITIONS_SERIALIZERS.register("predicate", PredicateCondition.Serializer::new);

}