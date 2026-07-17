package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ActionOnBeingUsedAbility;
import net.alkanphel.kryptonite.power.ability.ActionOnEntityUseAbility;
import net.alkanphel.kryptonite.power.ability.PreventBeingUsedAbility;
import net.alkanphel.kryptonite.power.ability.PreventEntityUseAbility;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionResultUtil;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.alkanphel.kryptonite.util.apoli.ability.PriorityPhase;
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
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Player.class, priority = 999)
public abstract class PlayerMixin extends LivingEntity {

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    // Prevent Gliding ability
    @Inject(method = "tryToStartFallFlying", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventGlidingII(CallbackInfoReturnable<Boolean> cir) {
        Player player = (Player) (Object) this;

        if (AbilityUtil.isTypeEnabled(player, KryptoniteAbilitySerializers.PREVENT_GLIDING.get())) {
            cir.setReturnValue(false);
        }
    }

    // Action On Entity Use & Action On Being Used & Prevent Entity Use & Prevent Being Used ability
    @WrapOperation(method = "interactOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"))
    private InteractionResult kryptonite$beforeEntityUse(Entity entity, Player player, InteractionHand hand, Vec3 location, Operation<InteractionResult> original, @Share("zeroPriority$onEntity") LocalRef<InteractionResult> sharedZeroPriority$onEntity) {
        ItemStack stackInHand = player.getItemInHand(hand);

        // Prevent Entity Use ability
        for (AbilityInstance<PreventEntityUseAbility> instance : AbilityUtil.getEnabledInstances(this, KryptoniteAbilitySerializers.PREVENT_ENTITY_USE.get())) {
            PreventEntityUseAbility preventEntityUse = instance.getAbility();
            if (preventEntityUse.doesApply(player, entity, hand, stackInHand)) {
                return preventEntityUse.runActions(player, entity, hand);
            }
        }

        // Prevent Being Used ability
        for (AbilityInstance<PreventBeingUsedAbility> instance : AbilityUtil.getEnabledInstances((LivingEntity) entity, KryptoniteAbilitySerializers.PREVENT_BEING_USED.get())) {
            PreventBeingUsedAbility preventBeingUsed = instance.getAbility();
            if (preventBeingUsed.doesApply(player, entity, hand, stackInHand)) {
                return preventBeingUsed.runActions(player, entity, hand);
            }
        }

        Prioritized.CallInstance<InteractionPrioritizedAbility> callInstance = new Prioritized.CallInstance<>();
        callInstance.add(player, ActionOnEntityUseAbility.class, a -> a.shouldRun(player, entity, hand, stackInHand, PriorityPhase.BEFORE));

        if (entity instanceof LivingEntity livingEntity) {
            callInstance.add(livingEntity, ActionOnBeingUsedAbility.class, a -> a.shouldRun(player, entity, hand, stackInHand, PriorityPhase.BEFORE));
        }

        InteractionResult[] earlyResult = { null };

        callInstance.forEachBucketEntryUntil((priority, interactPriorityAbilities) -> {
            InteractionResult previousResult = InteractionResult.PASS;

            for (InteractionPrioritizedAbility interactPriorityAbility : interactPriorityAbilities) {
                InteractionResult currentResult = InteractionResult.PASS;

                if (interactPriorityAbility instanceof ActionOnEntityUseAbility actionOnEntityUse) {
                    currentResult = actionOnEntityUse.runActions(player, entity, hand);
                } else if (interactPriorityAbility instanceof ActionOnBeingUsedAbility actionOnBeingUsed) {
                    currentResult = actionOnBeingUsed.runActions(player, entity, hand);
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

    // Action On Entity Use ability & Action On Being Used ability
    @ModifyReturnValue(method = "interactOn", at = @At("RETURN"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;", ordinal = 0)))
    private InteractionResult kryptonite$afterEntityUse(InteractionResult original, Entity entity, InteractionHand hand, @Share("zeroPriority$onEntity") LocalRef<InteractionResult> sharedZeroPriority$onEntity) {
        InteractionResult cachedPriorityZeroResult = sharedZeroPriority$onEntity.get();
        InteractionResult newResult = InteractionResult.PASS;

        if (cachedPriorityZeroResult != null && cachedPriorityZeroResult != InteractionResult.PASS) {
            newResult = cachedPriorityZeroResult;
        }

        else if (original == InteractionResult.PASS) {
            ItemStack stackInHand = this.getItemInHand(hand);

            Prioritized.CallInstance<InteractionPrioritizedAbility> callInstance = new Prioritized.CallInstance<>();
            callInstance.add(this, ActionOnEntityUseAbility.class, a -> a.shouldRun(this, entity, hand, stackInHand, PriorityPhase.AFTER));

            if (entity instanceof LivingEntity livingEntity) {
                callInstance.add(livingEntity, ActionOnBeingUsedAbility.class, a -> a.shouldRun((Player) (Object) this, entity, hand, stackInHand, PriorityPhase.AFTER));
            }

            InteractionResult[] trackingResult = { InteractionResult.PASS };

            callInstance.forEachBucketUntil(interactPriorityAbilities -> {
                InteractionResult previousResult = InteractionResult.PASS;

                for (InteractionPrioritizedAbility interactPriorityAbility : interactPriorityAbilities) {
                    InteractionResult currentResult = InteractionResult.PASS;

                    if (interactPriorityAbility instanceof ActionOnEntityUseAbility actionOnEntityUse) {
                        currentResult = actionOnEntityUse.runActions(this, entity, hand);
                    } else if (interactPriorityAbility instanceof ActionOnBeingUsedAbility actionOnBeingUsed) {
                        currentResult = actionOnBeingUsed.runActions((Player) (Object) this, entity, hand);
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

}