package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.network.p2c.S2CSyncAttacker;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ModifyFrictionAbility;
import net.alkanphel.kryptonite.power.ability.PreventDamageAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {

    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow @Nullable private LivingEntity lastHurtMob;

    // S2C Sync Attacker packet
    @Inject(method = "setLastHurtByMob", at = @At("TAIL"))
    private void kryptonite$syncAttacker(LivingEntity hurtBy, CallbackInfo ci) {
        if (this.level().isClientSide()) return;

        Optional<Integer> attackerId = Optional.ofNullable(this.lastHurtMob).map(Entity::getId);
        S2CSyncAttacker packet = new S2CSyncAttacker(this.getId(), attackerId);

        PacketDistributor.sendToPlayersTrackingEntityAndSelf(this, packet);
    }

    // Prevent Gliding ability
    @Inject(method = "updateFallFlying", at = @At("HEAD"))
    private void kryptonite$preventGlidingI(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;

        if (living.isFallFlying() && AbilityUtil.isTypeEnabled(living, KryptoniteAbilitySerializers.PREVENT_GLIDING.get())) {
            ((EntityAccessor) living).kryptonite$setSharedFlag(7, false);
        }
    }

    // Modify Friction ability
    @Redirect(method = "travelInAir", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getFriction(Lnet/minecraft/world/level/LevelReader;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/Entity;)F"))
    private float kryptonite$modifyFriction(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos, net.minecraft.world.entity.Entity entity) {
        float original = state.getFriction(level, pos, entity);
        if (!(entity instanceof LivingEntity living)) return original;
        return ModifyFrictionAbility.modifyFriction(living, pos, original);
    }

    // Prevent Damage ability (prevent freeze)
    @Inject(method = "canFreeze", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventDamagePreventFreeze(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity living = (LivingEntity) (Object) this;

        if (PreventDamageAbility.preventsFreeze(living)) {
            cir.setReturnValue(false);
        }
    }

}