package net.alkanphel.kryptonite.client.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;

public interface OpacityRenderChanging<T extends Ability> {

    float getRenderOpacity(LivingEntity viewer, Entity target, AbilityInstance<T> instance, float partialTick);

    @SuppressWarnings({"rawtypes", "unchecked"})
    static float getOpacity(LivingEntity viewer, Entity target, float partialTick) {
        float opacity = 1F;

        for (AbilityInstance ability : AbilityUtil.getInstances(viewer)) {
            if (ability.getAbility() instanceof OpacityRenderChanging<?> renderOpacityChanging) {
                opacity = Math.min(opacity, renderOpacityChanging.getRenderOpacity(viewer, target, ability, partialTick));
            }
        }

        return opacity;
    }

    static boolean isFullyHidden(LivingEntity viewer, Entity target, float partialTick) {
        return getOpacity(viewer, target, partialTick) <= 0F;
    }

}