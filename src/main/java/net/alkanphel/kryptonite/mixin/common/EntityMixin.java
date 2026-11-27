package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.*;
import net.alkanphel.kryptonite.util.apoli.access.EntityLinkedType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @ModifyReturnValue(method = "getType", at = @At("RETURN"))
    private EntityType<?> kryptonite$modifyTypeTag(EntityType<?> original) {
        if (original instanceof EntityLinkedType linkedType) {
            linkedType.kryptonite$setEntity((Entity) (Object) this);
        }

        return original;
    }

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

    // Prevent Slowdown (water) ability
    @Inject(method = "isInWater", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventSlowdownWaterIgnoreIsInWater(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof LivingEntity living)) return;
        if (PreventSlowdownAbility.preventsMode(living, PreventSlowdownAbility.Mode.WATER)) {
            cir.setReturnValue(false);
        }
    }

    // Intangibility ability
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;containing(DDD)Lnet/minecraft/core/BlockPos;"), method = "moveTowardsClosestSpace", cancellable = true)
    protected void kryptonite$pushOutOfBlocks(double x, double y, double z, CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity living) {
            for (AbilityInstance<IntangibilityAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.INTANGIBILITY.get())) {
                if (instance.getAbility().doesApply(living, BlockPos.containing(x, y, z))) {
                    ci.cancel();
                    return;
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    // Prevent Particles (Sprinting) ability
    @ModifyReturnValue(method = "canSpawnSprintParticle", at = @At("TAIL"))
    private boolean kryptonite$preventParticlesSprinting(boolean original) {
        if (!((Object) this instanceof LivingEntity living)) return original;

        return original && !PreventParticlesAbility.prevents(living, PreventParticlesAbility.EventParticle.SPRINTING);
    }

}