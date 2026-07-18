package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteAbilitySerializers {

    public static final DeferredRegister<AbilitySerializer<?>> ABILITIES_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.ABILITY_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<AbilitySerializer<?>, ActionOnBeingUsedAbility.Serializer> ACTION_ON_BEING_USED = ABILITIES_SERIALIZERS.register("action_on_being_used", ActionOnBeingUsedAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnBlockBreakAbility.Serializer> ACTION_ON_BLOCK_BREAK = ABILITIES_SERIALIZERS.register("action_on_block_break", ActionOnBlockBreakAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnBlockPlaceAbility.Serializer> ACTION_ON_BLOCK_PLACE = ABILITIES_SERIALIZERS.register("action_on_block_place", ActionOnBlockPlaceAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnBlockUseAbility.Serializer> ACTION_ON_BLOCK_USE = ABILITIES_SERIALIZERS.register("action_on_block_use", ActionOnBlockUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnCriticalHitAbility.Serializer> ACTION_ON_CRITICAL_HIT = ABILITIES_SERIALIZERS.register("action_on_critical_hit", ActionOnCriticalHitAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnDeathAbility.Serializer> ACTION_ON_DEATH = ABILITIES_SERIALIZERS.register("action_on_death", ActionOnDeathAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnEntityCollisionAbility.Serializer> ACTION_ON_ENTITY_COLLISION = ABILITIES_SERIALIZERS.register("action_on_entity_collision", ActionOnEntityCollisionAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnEntityUseAbility.Serializer> ACTION_ON_ENTITY_USE = ABILITIES_SERIALIZERS.register("action_on_entity_use", ActionOnEntityUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnFarmlandTrampleAbility.Serializer> ACTION_ON_FARMLAND_TRAMPLE = ABILITIES_SERIALIZERS.register("action_on_farmland_trample", ActionOnFarmlandTrampleAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnHitAbility.Serializer> ACTION_ON_HIT = ABILITIES_SERIALIZERS.register("action_on_hit", ActionOnHitAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnItemDropAbility.Serializer> ACTION_ON_ITEM_DROP = ABILITIES_SERIALIZERS.register("action_on_item_drop", ActionOnItemDropAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnItemFishedAbility.Serializer> ACTION_ON_ITEM_FISHED = ABILITIES_SERIALIZERS.register("action_on_item_fished", ActionOnItemFishedAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnItemPickupAbility.Serializer> ACTION_ON_ITEM_PICKUP = ABILITIES_SERIALIZERS.register("action_on_item_pickup", ActionOnItemPickupAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnItemSwapAbility.Serializer> ACTION_ON_ITEM_SWAP = ABILITIES_SERIALIZERS.register("action_on_item_swap", ActionOnItemSwapAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnItemUseAbility.Serializer> ACTION_ON_ITEM_USE = ABILITIES_SERIALIZERS.register("action_on_item_use", ActionOnItemUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnTameAbility.Serializer> ACTION_ON_TAME = ABILITIES_SERIALIZERS.register("action_on_tame", ActionOnTameAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnTotemUseAbility.Serializer> ACTION_ON_TOTEM_USE = ABILITIES_SERIALIZERS.register("action_on_totem_use", ActionOnTotemUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnWakeUpAbility.Serializer> ACTION_ON_WAKE_UP = ABILITIES_SERIALIZERS.register("action_on_wake_up", ActionOnWakeUpAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnJumpAbility.Serializer> ACTION_ON_JUMP = ABILITIES_SERIALIZERS.register("action_on_jump", ActionOnJumpAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnLandAbility.Serializer> ACTION_ON_LAND = ABILITIES_SERIALIZERS.register("action_on_land", ActionOnLandAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnMountAbility.Serializer> ACTION_ON_MOUNT = ABILITIES_SERIALIZERS.register("action_on_mount", ActionOnMountAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionWhenDamageTakenAbility.Serializer> ACTION_WHEN_DAMAGE_TAKEN = ABILITIES_SERIALIZERS.register("action_when_damage_taken", ActionWhenDamageTakenAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionWhenHitAbility.Serializer> ACTION_WHEN_HIT = ABILITIES_SERIALIZERS.register("action_when_hit", ActionWhenHitAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, AllowEndermanStareAbility.Serializer> ALLOW_ENDERMAN_STARE = ABILITIES_SERIALIZERS.register("allow_enderman_stare", AllowEndermanStareAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, DynamicLightsAbility.Serializer> DYNAMIC_LIGHTS = ABILITIES_SERIALIZERS.register("dynamic_lights", DynamicLightsAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, GlowingAbility.Serializer> GLOWING = ABILITIES_SERIALIZERS.register("glowing", GlowingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ImmediateRespawnAbility.Serializer> IMMEDIATE_RESPAWN = ABILITIES_SERIALIZERS.register("immediate_respawn", ImmediateRespawnAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyBlockDestroySpeedAbility.Serializer> MODIFY_BLOCK_DESTROY_SPEED = ABILITIES_SERIALIZERS.register("modify_block_destroy_speed", ModifyBlockDestroySpeedAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyBlockHarvestAbility.Serializer> MODIFY_BLOCK_HARVEST = ABILITIES_SERIALIZERS.register("modify_block_harvest", ModifyBlockHarvestAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyDamageDealtAbility.Serializer> MODIFY_DAMAGE_DEALT = ABILITIES_SERIALIZERS.register("modify_damage_dealt", ModifyDamageDealtAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyDamageTakenAbility.Serializer> MODIFY_DAMAGE_TAKEN = ABILITIES_SERIALIZERS.register("modify_damage_taken", ModifyDamageTakenAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyDamageTintAbility.Serializer> MODIFY_DAMAGE_TINT = ABILITIES_SERIALIZERS.register("modify_damage_tint", ModifyDamageTintAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyEffectsAbility.Serializer> MODIFY_EFFECTS = ABILITIES_SERIALIZERS.register("modify_effects", ModifyEffectsAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyFogTypeAbility.Serializer> MODIFY_FOG_TYPE = ABILITIES_SERIALIZERS.register("modify_fog_type", ModifyFogTypeAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyFrictionAbility.Serializer> MODIFY_FRICTION = ABILITIES_SERIALIZERS.register("modify_friction", ModifyFrictionAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyHealingAbility.Serializer> MODIFY_HEALING = ABILITIES_SERIALIZERS.register("modify_healing", ModifyHealingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyInvulnerabilityTicksAbility.Serializer> MODIFY_INVULNERABILITY_TICKS = ABILITIES_SERIALIZERS.register("modify_invulnerability_ticks", ModifyInvulnerabilityTicksAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ModifyKnockbackAbility.Serializer> MODIFY_KNOCKBACK = ABILITIES_SERIALIZERS.register("modify_knockback", ModifyKnockbackAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventBeingUsedAbility.Serializer> PREVENT_BEING_USED = ABILITIES_SERIALIZERS.register("prevent_being_used", PreventBeingUsedAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventBlockPlaceAbility.Serializer> PREVENT_BLOCK_PLACE = ABILITIES_SERIALIZERS.register("prevent_block_place", PreventBlockPlaceAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventBlockSelectionAbility.Serializer> PREVENT_BLOCK_SELECTION = ABILITIES_SERIALIZERS.register("prevent_block_selection", PreventBlockSelectionAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventBlockUseAbility.Serializer> PREVENT_BLOCK_USE = ABILITIES_SERIALIZERS.register("prevent_block_use", PreventBlockUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventCriticalHitAbility.Serializer> PREVENT_CRITICAL_HIT = ABILITIES_SERIALIZERS.register("prevent_critical_hit", PreventCriticalHitAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventDamageAbility.Serializer> PREVENT_DAMAGE = ABILITIES_SERIALIZERS.register("prevent_damage", PreventDamageAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventDeathAbility.Serializer> PREVENT_DEATH = ABILITIES_SERIALIZERS.register("prevent_death", PreventDeathAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventEffectsAbility.Serializer> PREVENT_EFFECTS = ABILITIES_SERIALIZERS.register("prevent_effects", PreventEffectsAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventEntityCollisionAbility.Serializer> PREVENT_ENTITY_COLLISION = ABILITIES_SERIALIZERS.register("prevent_entity_collision", PreventEntityCollisionAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventEntityRenderAbility.Serializer> PREVENT_ENTITY_RENDER = ABILITIES_SERIALIZERS.register("prevent_entity_render", PreventEntityRenderAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventEntitySelectionAbility.Serializer> PREVENT_ENTITY_SELECTION = ABILITIES_SERIALIZERS.register("prevent_entity_selection", PreventEntitySelectionAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventEntityUseAbility.Serializer> PREVENT_ENTITY_USE = ABILITIES_SERIALIZERS.register("prevent_entity_use", PreventEntityUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventFarmlandTrampleAbility.Serializer> PREVENT_FARMLAND_TRAMPLE = ABILITIES_SERIALIZERS.register("prevent_farmland_trample", PreventFarmlandTrampleAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventGameEventAbility.Serializer> PREVENT_GAME_EVENT = ABILITIES_SERIALIZERS.register("prevent_game_event", PreventGameEventAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventGlidingAbility.Serializer> PREVENT_GLIDING = ABILITIES_SERIALIZERS.register("prevent_gliding", PreventGlidingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventHealingAbility.Serializer> PREVENT_HEALING = ABILITIES_SERIALIZERS.register("prevent_healing", PreventHealingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventItemUseAbility.Serializer> PREVENT_ITEM_USE = ABILITIES_SERIALIZERS.register("prevent_item_use", PreventItemUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventItemPickupAbility.Serializer> PREVENT_ITEM_PICKUP = ABILITIES_SERIALIZERS.register("prevent_item_pickup", PreventItemPickupAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventMobAggroAbility.Serializer> PREVENT_MOB_AGGRO = ABILITIES_SERIALIZERS.register("prevent_mob_aggro", PreventMobAggroAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventSleepingAbility.Serializer> PREVENT_SLEEPING = ABILITIES_SERIALIZERS.register("prevent_sleeping", PreventSleepingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventSprintingAbility.Serializer> PREVENT_SPRINTING = ABILITIES_SERIALIZERS.register("prevent_sprinting", PreventSprintingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventTotemUseAbility.Serializer> PREVENT_TOTEM_USE = ABILITIES_SERIALIZERS.register("prevent_totem_use", PreventTotemUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ProjectileAccuracyAbility.Serializer> PROJECTILE_ACCURACY = ABILITIES_SERIALIZERS.register("projectile_accuracy", ProjectileAccuracyAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ProjectileImpactAbility.Serializer> PROJECTILE_IMPACT = ABILITIES_SERIALIZERS.register("projectile_impact", ProjectileImpactAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ShakingAbility.Serializer> SHAKING = ABILITIES_SERIALIZERS.register("shaking", ShakingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, StepDownAbility.Serializer> STEP_DOWN = ABILITIES_SERIALIZERS.register("step_down", StepDownAbility.Serializer::new);

}