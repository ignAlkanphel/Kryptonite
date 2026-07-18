package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.network.p2c.S2CSyncAttacker;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ModifyEffectsAbility;
import net.alkanphel.kryptonite.power.ability.ModifyFrictionAbility;
import net.alkanphel.kryptonite.power.ability.PreventDamageAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
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

    // Modify Effects ability
    @ModifyVariable(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"), argsOnly = true, name = "newEffect")
    private MobEffectInstance kryptonite$modifyEffects(MobEffectInstance newEffect) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return newEffect;

        Holder<MobEffect> effectType = newEffect.getEffect();

        int newAmplifier = newEffect.getAmplifier();
        int newDuration = newEffect.getDuration();

        for (AbilityInstance<ModifyEffectsAbility> instance : AbilityUtil.getEnabledInstances(self, KryptoniteAbilitySerializers.MODIFY_EFFECTS.get())) {
            ModifyEffectsAbility ability = instance.getAbility();

            if (!ability.doesApply(effectType)) {
                continue;
            }

            switch (ability.mode) {
                case AMPLIFIER -> newAmplifier = ability.applyModifiers(newAmplifier, self, instance);
                case DURATION -> newDuration = ability.applyModifiers(newDuration, self, instance);
            }
        }

        if (newAmplifier == newEffect.getAmplifier() && newDuration == newEffect.getDuration()) {
            return newEffect;
        }

        return new MobEffectInstance(effectType, newDuration, newAmplifier, newEffect.isAmbient(), newEffect.isVisible(), newEffect.showIcon());
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