package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.power.ability.GlowingAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {

    @Shadow public LocalPlayer player;

    // Glowing ability
    @Inject(method = "shouldEntityAppearGlowing", at = @At("RETURN"), cancellable = true)
    private void kryptonite$makeEntitiesGlow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || player == null) return;

        if (player != entity && GlowingAbility.shouldTargetGlow(player, entity)) {
            cir.setReturnValue(true);
            return;
        }

        if (entity instanceof LivingEntity livingEntity && GlowingAbility.shouldActorGlow(player, livingEntity)) {
            cir.setReturnValue(true);
        }
    }

}