package net.alkanphel.kryptonite.power.compat.lambdynlights;

import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.AbilityInstance;

public class DynLightsAbilityHandlerClient extends DynLightsAbilityHandler {
    @Override public void triggerLight(LivingEntity entity, AbilityInstance<?> abilityInstance, Value luminance) {}
}