package net.alkanphel.kryptonite.power.compat.lambdynlights;

import dev.lambdaurora.lambdynlights.api.entity.luminance.EntityLuminance;
import dev.lambdaurora.lambdynlights.api.item.ItemLightSourceManager;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.DynamicLightsAbility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;

public final class DynLightsAbilityLuminance implements EntityLuminance {

    public static final DynLightsAbilityLuminance INSTANCE = new DynLightsAbilityLuminance();
    private DynLightsAbilityLuminance() {}

    @Override
    public Type type() {
        return KryptoniteDynamicLightsInitializer.ABILITY_LUMINANCE;
    }

    @Override
    public @Range(from = 0, to = 15) int getLuminance(@NotNull ItemLightSourceManager manager, @NotNull Entity entity) {
        if (!(entity instanceof LivingEntity living)) return 0;

        Collection<AbilityInstance<DynamicLightsAbility>> instances = AbilityUtil.getInstances(living, KryptoniteAbilitySerializers.DYNAMIC_LIGHTS.get());

        int maxLight = 0;

        for (AbilityInstance<DynamicLightsAbility> instance : instances) {
            DynamicLightsAbility ability = instance.getAbility();

            float scale = 1F;

            if (instance.getAnimationTimer() != null) {
                scale = instance.getAnimationTimerProgressEased(1F);
            } else if (!instance.isEnabled()) {
                scale = 0F;
            }

            float luminance = ability.luminance.getAsFloat(DataContext.forAbility(living, instance));
            int light = Math.round(luminance * scale);

            maxLight = Math.max(maxLight, light);
        }

        return Math.min(maxLight, 15);
    }

}