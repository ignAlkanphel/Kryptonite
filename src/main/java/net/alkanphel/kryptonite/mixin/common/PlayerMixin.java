package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventBeingUsedAbility;
import net.alkanphel.kryptonite.power.ability.PreventEntityUseAbility;
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

        return original.call(entity, player, hand, location);
    }

}