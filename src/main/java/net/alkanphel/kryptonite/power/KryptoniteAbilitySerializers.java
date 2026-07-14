package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class KryptoniteAbilitySerializers {
    public static final DeferredRegister<AbilitySerializer<?>> ABILITIES_SERIALIZERS = DeferredRegister.create(PalladiumRegistryKeys.ABILITY_SERIALIZER, Kryptonite.MOD_ID);

    public static final DeferredHolder<AbilitySerializer<?>, DynamicLightsAbility.Serializer> DYNAMIC_LIGHTS = ABILITIES_SERIALIZERS.register("dynamic_lights", DynamicLightsAbility.Serializer::new);

}