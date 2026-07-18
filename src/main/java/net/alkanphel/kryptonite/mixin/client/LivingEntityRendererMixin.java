package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.alkanphel.kryptonite.client.render.KryptoniteLivingEntityRenderState;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ModifyDamageTintAbility;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @Unique
    private LivingEntity kryptonite$currentEntity;

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("HEAD"))
    private void kryptonite$captureEntity(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        this.kryptonite$currentEntity = entity;
    }

    // Shaking ability
    @Inject(method = "setupRotations(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;FF)V", at = @At("HEAD"))
    private void kryptonite$applyShaking(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float entityScale, CallbackInfo ci) {
        LivingEntity entity = this.kryptonite$currentEntity;
        if (entity == null) return;

        var instances = AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.SHAKING.get());
        if (instances.isEmpty()) return;

        float frequency = 0F;
        float amplitude = 0F;

        for (var instance : instances) {
            DataContext context = DataContext.forAbility(entity, instance);
            frequency = Math.max(frequency, (float) instance.getAbility().frequency.getAsDouble(context));
            amplitude = Math.max(amplitude, (float) instance.getAbility().amplitude.getAsDouble(context));
        }

        if (frequency <= 0F || amplitude <= 0F) return;

        float time = state.ageInTicks + state.partialTick;
        float shake = (float) (Math.cos(time * frequency) * Math.PI * amplitude);

        poseStack.mulPose(Axis.YP.rotationDegrees(-shake));
    }

    // Modify Damage Tint ability
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void kryptonite$modifyDamageTint(LivingEntity entity, LivingEntityRenderState state, float partialTicks, CallbackInfo ci) {
        if (state instanceof KryptoniteLivingEntityRenderState renderStateExtension) {
            Collection<AbilityInstance<ModifyDamageTintAbility>> tintInstances = AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.MODIFY_DAMAGE_TINT.get());

            if (!tintInstances.isEmpty()) {
                AbilityInstance<ModifyDamageTintAbility> instance = tintInstances.iterator().next();
                ModifyDamageTintAbility ability = instance.getAbility();
                DataContext context = DataContext.forAbility(entity, instance);

                float r = (float) ability.color.red().getAsDouble(context);
                float g = (float) ability.color.green().getAsDouble(context);
                float b = (float) ability.color.blue().getAsDouble(context);
                float alphaFactor = Math.max(0.0F, Math.min(1.0F, (float) ability.color.alpha().getAsDouble(context)));

                renderStateExtension.kryptonite$setDamageTintAlpha(alphaFactor);

                if (alphaFactor <= 0.0F) {
                    renderStateExtension.kryptonite$setDamageTint(-1);
                } else {
                    float finalR = r + (1.0F - r) * (1.0F - alphaFactor);
                    float finalG = g + (1.0F - g) * (1.0F - alphaFactor);
                    float finalB = b + (1.0F - b) * (1.0F - alphaFactor);

                    int red = (int) (Math.max(0.0F, Math.min(1.0F, finalR)) * 255);
                    int green = (int) (Math.max(0.0F, Math.min(1.0F, finalG)) * 255);
                    int blue = (int) (Math.max(0.0F, Math.min(1.0F, finalB)) * 255);

                    renderStateExtension.kryptonite$setDamageTint(ARGB.color(255, red, green, blue));
                }
            } else {
                renderStateExtension.kryptonite$setDamageTint(-1);
                renderStateExtension.kryptonite$setDamageTintAlpha(1.0F);
            }
        }
    }

    @Inject(method = "getModelTint(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)I", at = @At("HEAD"), cancellable = true)
    private void kryptonite$applyDamageTint(LivingEntityRenderState state, CallbackInfoReturnable<Integer> cir) {
        if (state.hasRedOverlay && state instanceof KryptoniteLivingEntityRenderState renderStateExtension) {
            int customTint = renderStateExtension.kryptonite$getDamageTint();
            if (customTint != -1) cir.setReturnValue(customTint);
        }
    }

    @ModifyReturnValue(method = "getOverlayCoords", at = @At("RETURN"))
    private static int kryptonite$modifyDamageTintAlpha(int original, LivingEntityRenderState state) {
        if (state.hasRedOverlay && state instanceof KryptoniteLivingEntityRenderState renderStateExtension) {
            if (renderStateExtension.kryptonite$getDamageTintAlpha() <= 0.0F) {
                return OverlayTexture.NO_OVERLAY;
            }

            if (renderStateExtension.kryptonite$getDamageTint() != -1) {
                return OverlayTexture.pack(0, OverlayTexture.RED_OVERLAY_V);
            }
        }

        return original;
    }

}