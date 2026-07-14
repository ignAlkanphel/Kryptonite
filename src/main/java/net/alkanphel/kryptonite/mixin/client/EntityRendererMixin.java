package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.DynamicLightsAbility;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity, S extends EntityRenderState> {

    // Dynamic Lights ability
    @WrapOperation(method = "getPackedLightCoords", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;getBlockLightLevel(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/BlockPos;)I"))
    private int kryptonite$dynLightsAbilityModel(EntityRenderer<?, ?> instance, Entity entity, BlockPos blockPos, Operation<Integer> original) {
        int originalLight = original.call(instance, entity, blockPos);

        if (!(entity instanceof LivingEntity living)) return originalLight;

        int maxLight = 0;

        for (AbilityInstance<DynamicLightsAbility> abilityInstance : AbilityUtil.getInstances(living, KryptoniteAbilitySerializers.DYNAMIC_LIGHTS.get())) {
            DynamicLightsAbility ability = abilityInstance.getAbility();

            float scale = 1F;

            if (abilityInstance.getAnimationTimer() != null) {
                scale = abilityInstance.getAnimationTimerProgressEased(1F);
            } else if (!abilityInstance.isEnabled()) {
                scale = 0F;
            }

            float modelLight = (float) ability.modelLight.getAsDouble(DataContext.forAbility(living, abilityInstance));
            int light = Math.round(modelLight * scale);

            maxLight = Math.max(maxLight, light);
        }

        if (maxLight <= 0) {
            return originalLight;
        }

        return Math.max(originalLight, Math.min(maxLight, 15));
    }

}