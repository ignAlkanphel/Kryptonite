package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteAbilitySerializers {

    public static final DeferredRegister<AbilitySerializer<?>> ABILITIES_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.ABILITY_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<AbilitySerializer<?>, ActionOnDeathAbility.Serializer> ACTION_ON_DEATH = ABILITIES_SERIALIZERS.register("action_on_death", ActionOnDeathAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnFarmlandTrampleAbility.Serializer> ACTION_ON_FARMLAND_TRAMPLE = ABILITIES_SERIALIZERS.register("action_on_farmland_trample", ActionOnFarmlandTrampleAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnItemFishedAbility.Serializer> ACTION_ON_ITEM_FISHED = ABILITIES_SERIALIZERS.register("action_on_item_fished", ActionOnItemFishedAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnJumpAbility.Serializer> ACTION_ON_JUMP = ABILITIES_SERIALIZERS.register("action_on_jump", ActionOnJumpAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnLandAbility.Serializer> ACTION_ON_LAND = ABILITIES_SERIALIZERS.register("action_on_land", ActionOnLandAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ActionOnTotemUseAbility.Serializer> ACTION_ON_TOTEM_USE = ABILITIES_SERIALIZERS.register("action_on_totem_use", ActionOnTotemUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, DynamicLightsAbility.Serializer> DYNAMIC_LIGHTS = ABILITIES_SERIALIZERS.register("dynamic_lights", DynamicLightsAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ImmediateRespawnAbility.Serializer> IMMEDIATE_RESPAWN = ABILITIES_SERIALIZERS.register("immediate_respawn", ImmediateRespawnAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventDamageAbility.Serializer> PREVENT_DAMAGE = ABILITIES_SERIALIZERS.register("prevent_damage", PreventDamageAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventDeathAbility.Serializer> PREVENT_DEATH = ABILITIES_SERIALIZERS.register("prevent_death", PreventDeathAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventFarmlandTrampleAbility.Serializer> PREVENT_FARMLAND_TRAMPLE = ABILITIES_SERIALIZERS.register("prevent_farmland_trample", PreventFarmlandTrampleAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventGlidingAbility.Serializer> PREVENT_GLIDING = ABILITIES_SERIALIZERS.register("prevent_gliding", PreventGlidingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventHealingAbility.Serializer> PREVENT_HEALING = ABILITIES_SERIALIZERS.register("prevent_healing", PreventHealingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventSprintingAbility.Serializer> PREVENT_SPRINTING = ABILITIES_SERIALIZERS.register("prevent_sprinting", PreventSprintingAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, PreventTotemUseAbility.Serializer> PREVENT_TOTEM_USE = ABILITIES_SERIALIZERS.register("prevent_totem_use", PreventTotemUseAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ProjectileAccuracyAbility.Serializer> PROJECTILE_ACCURACY = ABILITIES_SERIALIZERS.register("projectile_accuracy", ProjectileAccuracyAbility.Serializer::new);
    public static final DeferredHolder<AbilitySerializer<?>, ProjectileImpactAbility.Serializer> PROJECTILE_IMPACT = ABILITIES_SERIALIZERS.register("projectile_impact", ProjectileImpactAbility.Serializer::new);

}