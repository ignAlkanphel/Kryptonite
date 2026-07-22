package net.alkanphel.kryptonite.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.IntangibilityAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ScreenEffectRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ScreenEffectRenderer.class)
public class ScreenEffectRendererMixin {

    @Inject(method = "renderTex", at = @At("HEAD"), cancellable = true)
    private static void kryptonite$preventInWallOverlayRendering(TextureAtlasSprite sprite, PoseStack poseStack, MultiBufferSource bufferSource, CallbackInfo ci) {
        if (!(Minecraft.getInstance().getCameraEntity() instanceof LivingEntity living)) return;

        for (AbilityInstance<IntangibilityAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.INTANGIBILITY.get())) {
            var renderType = instance.getAbility().renderType;
            if (renderType == IntangibilityAbility.RenderType.NONE || renderType == IntangibilityAbility.RenderType.BLINDNESS) {
                ci.cancel();
                return;
            }
        }
    }

}