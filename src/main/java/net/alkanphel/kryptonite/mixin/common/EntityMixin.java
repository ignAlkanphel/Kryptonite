package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventDamageAbility;
import net.alkanphel.kryptonite.power.ability.PreventEntityCollisionAbility;
import net.alkanphel.kryptonite.power.ability.PreventParticlesAbility;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    // Action On Land ability
    @Inject(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;fallOn(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;D)V"))
    private void kryptonite$actionOnLand(double ya, boolean onGround, BlockState onState, BlockPos pos, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity living) {
            AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.ACTION_ON_LAND.get()).forEach(instance -> instance.getAbility().runActions(living));
        }
    }

    // Prevent Damage ability (prevent fire)
    @ModifyReturnValue(method = "fireImmune", at = @At("RETURN"))
    private boolean kryptonite$preventDamagePreventFire(boolean original) {
        Entity self = (Entity) (Object) this;

        return original || self instanceof LivingEntity living && PreventDamageAbility.preventsFire(living);
    }

    // Prevent Entity Collision ability
    @Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventEntityPushing(Entity entity, CallbackInfo ci) {
        if (PreventEntityCollisionAbility.doesApply((Entity) (Object) this, entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "isPickable", at = @At("RETURN"), cancellable = true)
    private void kryptonite$preventEntityCollision(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() && PreventEntityCollisionAbility.doesApply((Entity) (Object) this, (Entity) (Object) this)) {
            cir.setReturnValue(false);
        }
    }

    // Prevent Slowdown (water) ability
    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventSlowdownWaterIgnoreIsInWater(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof LivingEntity livingEntity)) return;

        if (AbilityUtil.getEnabledInstances(livingEntity, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get()).stream().anyMatch(i -> i.getAbility().modePrevents(PreventSlowdownAbility.Mode.WATER))) {
            cir.setReturnValue(false);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    // Prevent Particles (Sprinting) ability
    @ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("TAIL"))
    private boolean kryptonite$preventParticlesSprinting(boolean original) {
        if (!((Object) this instanceof Player player)) return original;
        if (AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_PARTICLES.get())
                .stream().anyMatch(i -> i.getAbility().prevents(PreventParticlesAbility.EventParticle.SPRINTING))) {
            return false;
        }

        return original;
    }

}