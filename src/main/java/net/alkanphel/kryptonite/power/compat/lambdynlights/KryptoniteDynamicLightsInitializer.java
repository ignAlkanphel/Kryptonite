package net.alkanphel.kryptonite.power.compat.lambdynlights;

import dev.lambdaurora.lambdynlights.api.DynamicLightsContext;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import dev.lambdaurora.lambdynlights.api.entity.EntityLightSource;
import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class KryptoniteDynamicLightsInitializer implements DynamicLightsInitializer {

    public static final EntityLuminance.Type ABILITY_LUMINANCE = EntityLuminance.Type.registerSimple(Kryptonite.id("ability_luminance"), DynLightsAbilityLuminance.INSTANCE);

    @Override
    public void onInitializeDynamicLights(DynamicLightsContext context) {
        context.entityLightSourceManager().onRegisterEvent()
                .register(registrar -> registrar.register(new EntityLightSource(EntityLightSource.EntityPredicate.builder().of(
                        BuiltInRegistries.ENTITY_TYPE, EntityType.PLAYER).build(),
                        List.of(DynLightsAbilityLuminance.INSTANCE)
                )
        ));
    }

}