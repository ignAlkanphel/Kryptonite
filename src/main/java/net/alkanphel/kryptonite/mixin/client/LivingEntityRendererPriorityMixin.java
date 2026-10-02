package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import net.alkanphel.kryptonite.client.render.KryptoniteLivingEntityRenderState;
import net.alkanphel.kryptonite.power.attribute.KryptoniteAttributeUtil;
import net.alkanphel.kryptonite.power.attribute.KryptoniteAttributeUtilClient;
import net.alkanphel.kryptonite.power.attribute.KryptoniteAttributes;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(value = LivingEntityRenderer.class, priority = 500)
public abstract class LivingEntityRendererPriorityMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {

    @ModifyArgs(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V", ordinal = 1))
    private void kryptonite$applyModelDepth(Args args, @Local(argsOnly = true, name = "state") LivingEntityRenderState state) {
        float z = state.scale;
        if (z != 0.0F) args.set(2, (float) (KryptoniteAttributeUtilClient.getModelDepth(state, z) / z));
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void kryptonite$storeModelDepth(T entity, S state, float partialTicks, CallbackInfo ci) {
        ((KryptoniteLivingEntityRenderState) state).kryptonite$setModelDepth(KryptoniteAttributeUtil.getScaleAttributeModifierValue(entity, KryptoniteAttributes.MODEL_DEPTH));
    }

}