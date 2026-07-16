package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.power.ability.ModifyFogTypeAbility;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class GameRendererMixin {

    // Modify Fog Type ability
    @Redirect(method = "extractCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;getFluidInCamera()Lnet/minecraft/world/level/material/FogType;"))
    private FogType kryptonite$modifyFluidFogTypeFov(Camera camera) {
        FogType original = camera.getFluidInCamera();
        if (camera.entity() instanceof LivingEntity living) {
            return ModifyFogTypeAbility.tryReplace(living, original).orElse(original);
        }
        return original;
    }

}