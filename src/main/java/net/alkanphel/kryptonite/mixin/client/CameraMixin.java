package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.power.ability.ModifyFogTypeAbility;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(Camera.class)
public class CameraMixin {

    @Shadow private Entity entity;

    // Modify Fog Type ability
    @Inject(method = "getFluidInCamera", at = @At("RETURN"), cancellable = true)
    private void kryptonite$modifyFluidFogType(CallbackInfoReturnable<FogType> cir) {
        if (this.entity instanceof LivingEntity living) {
            FogType originalType = cir.getReturnValue();

            Optional<FogType> replacement = ModifyFogTypeAbility.tryReplace(living, originalType);
            replacement.ifPresent(cir::setReturnValue);
        }
    }

}