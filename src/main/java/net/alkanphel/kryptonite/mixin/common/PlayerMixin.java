package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteAttachments;
import net.alkanphel.kryptonite.power.ability.*;
import net.alkanphel.kryptonite.util.AttachmentUtil;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionResultUtil;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.alkanphel.kryptonite.util.apoli.ability.PriorityPhase;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantValue")
@Mixin(value = Player.class, priority = 999)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // Keep Inventory ability
    @Inject(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"))
    private void kryptonite$preventKeptItemsFromDropping(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        for (AbilityInstance<KeepInventoryAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.KEEP_INVENTORY.get())) {
            instance.getAbility().preventItemsFromDropping(player);
        }
    }

    @Inject(method = "dropEquipment", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V", shift = At.Shift.AFTER))
    private void kryptonite$restoreKeptItems(CallbackInfo ci) {
        Player player = (Player) (Object) this;
        for (AbilityInstance<KeepInventoryAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.KEEP_INVENTORY.get())) {
            instance.getAbility().restoreSavedItems(player);
        }
    }

    // Keep Inventory attachment
    @ModifyExpressionValue(method = "dropEquipment", at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;booleanValue()Z"))
    private boolean kryptonite$dropEquipmentKeepInventory(boolean original) {
        return original || AttachmentUtil.getBoolean((Player) (Object) this, KryptoniteAttachments.Addon.KEEP_INVENTORY);
    }

    @ModifyExpressionValue(method = "getBaseExperienceReward", at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;booleanValue()Z"))
    private boolean kryptonite$experienceRewardKeepInventory(boolean original) {
        return original || AttachmentUtil.getBoolean((Player) (Object) this, KryptoniteAttachments.Addon.KEEP_INVENTORY);
    }

    // Action On Entity Use & Prevent Entity Use ability
    @WrapOperation(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult kryptonite$beforeEntityUse(Entity entity, Player player, InteractionHand hand, Vec3 location, Operation<InteractionResult> original, @Share("zeroPriority$onEntity") LocalRef<InteractionResult> sharedZeroPriority$onEntity) {
        ItemStack stackInHand = player.getItemInHand(hand);

        // Prevent Entity Use ability (actor)
        for (AbilityInstance<PreventEntityUseAbility> instance : AbilityUtil.getEnabledInstances(this, KryptoniteAbilitySerializers.PREVENT_ENTITY_USE.get())) {
            PreventEntityUseAbility preventUse = instance.getAbility();
            if (preventUse.doesApply(PreventEntityUseAbility.TargetUsed.ACTOR, player, entity, hand, stackInHand)) {
                return preventUse.runActions(player, entity, hand);
            }
        }

        // Prevent Entity Use ability (target)
        if (entity instanceof LivingEntity living) {
            for (AbilityInstance<PreventEntityUseAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_ENTITY_USE.get())) {
                PreventEntityUseAbility preventUse = instance.getAbility();
                if (preventUse.doesApply(PreventEntityUseAbility.TargetUsed.TARGET, player, entity, hand, stackInHand)) {
                    return preventUse.runActions(player, entity, hand);
                }
            }
        }

        // Action On Entity Use ability
        Prioritized.CallInstance<InteractionPrioritizedAbility> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(player, ActionOnEntityUseAbility.class, a -> a.shouldRun(ActionOnEntityUseAbility.TargetUsed.ACTOR, player, entity, hand, stackInHand, PriorityPhase.BEFORE));

        if (entity instanceof LivingEntity livingEntity) {
            callInstance.add(livingEntity, ActionOnEntityUseAbility.class, a -> a.shouldRun(ActionOnEntityUseAbility.TargetUsed.TARGET, player, entity, hand, stackInHand, PriorityPhase.BEFORE));
        }

        InteractionResult[] earlyResult = { null };

        callInstance.forEachBucketEntryUntil((priority, interactPriorityAbilities) -> {
            InteractionResult previousResult = InteractionResult.PASS;

            for (InteractionPrioritizedAbility interactPriorityAbility : interactPriorityAbilities) {
                InteractionResult currentResult = InteractionResult.PASS;

                if (interactPriorityAbility instanceof ActionOnEntityUseAbility actionOnUse) {
                    currentResult = actionOnUse.runActions(player, entity, hand);
                }

                if (InteractionResultUtil.shouldOverride(previousResult, currentResult)) {
                    previousResult = currentResult;
                }
            }

            if (priority == 0) {
                sharedZeroPriority$onEntity.set(previousResult);
                return false;
            }

            if (previousResult == InteractionResult.PASS) {
                return false;
            }

            earlyResult[0] = previousResult;
            return true;
        });

        if (earlyResult[0] != null) {
            if (InteractionResultUtil.shouldSwingHand(earlyResult[0])) {
                this.swing(hand);
            }

            return earlyResult[0];
        }

        return original.call(entity, player, hand, location);
    }

    // Action On Entity Use ability
    @ModifyReturnValue(method = "interactOn", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0)))
    private InteractionResult kryptonite$afterEntityUse(InteractionResult original, Entity entity, InteractionHand hand, @Share("zeroPriority$onEntity") LocalRef<InteractionResult> sharedZeroPriority$onEntity) {
        InteractionResult cachedPriorityZeroResult = sharedZeroPriority$onEntity.get();
        InteractionResult newResult = InteractionResult.PASS;

        if (cachedPriorityZeroResult != null && cachedPriorityZeroResult != InteractionResult.PASS) {
            newResult = cachedPriorityZeroResult;
        }

        else if (original == InteractionResult.PASS) {
            ItemStack stackInHand = this.getItemInHand(hand);
            Player player = (Player) (Object) this;

            Prioritized.CallInstance<InteractionPrioritizedAbility> callInstance = new Prioritized.CallInstance<>();
            callInstance.add(player, ActionOnEntityUseAbility.class, a -> a.shouldRun(ActionOnEntityUseAbility.TargetUsed.ACTOR, player, entity, hand, stackInHand, PriorityPhase.AFTER));

            if (entity instanceof LivingEntity livingEntity) {
                callInstance.add(livingEntity, ActionOnEntityUseAbility.class, a -> a.shouldRun(ActionOnEntityUseAbility.TargetUsed.TARGET, player, entity, hand, stackInHand, PriorityPhase.AFTER));
            }

            InteractionResult[] trackingResult = { InteractionResult.PASS };

            callInstance.forEachBucketUntil(interactPriorityAbilities -> {
                InteractionResult previousResult = InteractionResult.PASS;

                for (InteractionPrioritizedAbility interactPriorityAbility : interactPriorityAbilities) {
                    InteractionResult currentResult = InteractionResult.PASS;

                    if (interactPriorityAbility instanceof ActionOnEntityUseAbility actionOnUse) {
                        currentResult = actionOnUse.runActions(player, entity, hand);
                    }

                    if (InteractionResultUtil.shouldOverride(previousResult, currentResult)) {
                        previousResult = currentResult;
                    }
                }

                if (previousResult != InteractionResult.PASS) {
                    trackingResult[0] = previousResult;
                    return true;
                }

                return false;
            });

            newResult = trackingResult[0];
        }

        if (InteractionResultUtil.shouldSwingHand(newResult)) {
            this.swing(hand);
        }

        return InteractionResultUtil.shouldOverride(original, newResult) ? newResult : original;
    }

    // Prevent Slowdown (water) ability
    @Inject(method = "updateSwimming", at = @At("TAIL"))
    private void kryptonite$preventSlowdownWaterUpdateSwimming(CallbackInfo ci) {
        Player player = (Player) (Object) this;

        if (AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get()).stream().anyMatch(i -> i.getAbility().modePrevents(PreventSlowdownAbility.Mode.WATER))) {
            this.setSwimming(false);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------

    // Prevent Particles (Critical Hit) ability
    @Redirect(method = "attackVisualEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;crit(Lnet/minecraft/world/entity/Entity;)V"))
    private void kryptonite$preventParticlesCriticalHit(Player instance, Entity entity) {
        if (AbilityUtil.getEnabledInstances(instance, KryptoniteAbilitySerializers.PREVENT_PARTICLES.get())
                .stream().anyMatch(i -> i.getAbility().prevents(PreventParticlesAbility.EventParticle.CRITICAL_HIT))) {
            return;
        }

        instance.crit(entity);
    }

    // Prevent Particles (Magical Critical Hit) ability
    @Redirect(method = "attackVisualEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;magicCrit(Lnet/minecraft/world/entity/Entity;)V"))
    private void kryptonite$preventParticlesMagicCriticalHit(Player instance, Entity entity) {
        if (AbilityUtil.getEnabledInstances(instance, KryptoniteAbilitySerializers.PREVENT_PARTICLES.get())
                .stream().anyMatch(i -> i.getAbility().prevents(PreventParticlesAbility.EventParticle.MAGIC_CRITICAL_HIT))) {
            return;
        }
        instance.magicCrit(entity);
    }

    // Prevent Particles (Damage Indicator) ability
    @Redirect(method = "damageStatsAndHearts", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private <T extends ParticleOptions> int kryptonite$preventParticlesDamageIndicator(ServerLevel level, T particle, double x, double y, double z, int count, double xDist, double yDist, double zDist, double speed) {
        Player self = (Player)(Object)this;
        if (AbilityUtil.getEnabledInstances(self, KryptoniteAbilitySerializers.PREVENT_PARTICLES.get())
                .stream().anyMatch(i -> i.getAbility().prevents(PreventParticlesAbility.EventParticle.DAMAGE_INDICATOR))) {
            return 0;
        }

        return level.sendParticles(particle, x, y, z, count, xDist, yDist, zDist, speed);
    }

    // Prevent Particles (Sweep Attack) ability
    @Redirect(method = "doSweepAttack(Lnet/minecraft/world/entity/Entity;FLnet/minecraft/world/damagesource/DamageSource;FLnet/minecraft/world/phys/AABB;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private <T extends ParticleOptions> int kryptonite$preventParticlesSweepAttack(ServerLevel level, T particle, double x, double y, double z, int count, double xDist, double yDist, double zDist, double speed) {
        Player self = (Player)(Object)this;
        if (AbilityUtil.getEnabledInstances(self, KryptoniteAbilitySerializers.PREVENT_PARTICLES.get())
                .stream().anyMatch(i -> i.getAbility().prevents(PreventParticlesAbility.EventParticle.SWEEP_ATTACK))) {
            return 0;
        }

        return level.sendParticles(particle, x, y, z, count, xDist, yDist, zDist, speed);
    }

}