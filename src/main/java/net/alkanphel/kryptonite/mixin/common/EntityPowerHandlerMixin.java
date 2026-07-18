package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ActionOnCallbackAbility;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.power.EntityPowerHandler;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashSet;
import java.util.Set;

@Mixin(EntityPowerHandler.class)
public abstract class EntityPowerHandlerMixin {

    @Unique private final Set<Identifier> kryptonite$previousPowers = new HashSet<>();
    @Unique private final Set<ActionOnCallbackAbility> kryptonite$removedCallbacks = new HashSet<>();

    @Unique private boolean kryptonite$pendingRespawn = false;
    @Unique private boolean kryptonite$initializedPowers = false;

    @Inject(method = "tick", at = @At("HEAD"))
    private void kryptonite$preTick(CallbackInfo ci) {
        EntityPowerHandler self = (EntityPowerHandler)(Object)this;

        if (!self.getEntity().level().isClientSide()) {
            kryptonite$previousPowers.clear();
            kryptonite$removedCallbacks.clear();

            self.getPowers().forEach(instance -> kryptonite$previousPowers.add(instance.getPowerId()));
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void kryptonite$postTick(CallbackInfo ci) {
        EntityPowerHandler self = (EntityPowerHandler)(Object)this;

        LivingEntity entity = self.getEntity();
        if (entity.level().isClientSide()) return;

        kryptonite$initializedPowers = true;
        for (ActionOnCallbackAbility callback : kryptonite$removedCallbacks) {
            callback.onLoss(entity);
        }

        if (kryptonite$pendingRespawn && !self.getPowers().isEmpty()) {
            kryptonite$pendingRespawn = false;

            var callbacks = AbilityUtil.getInstances(entity, KryptoniteAbilitySerializers.ACTION_ON_CALLBACK.get());
            callbacks.forEach(ab -> ab.getAbility().onSpawn(entity));
        }
    }

    @Inject(method = "addPowerInstance", at = @At("TAIL"), remap = false)
    private void kryptonite$onPowerAdded(PowerInstance instance, CallbackInfo ci) {
        LivingEntity entity = ((EntityPowerHandler) (Object) this).getEntity();
        if (entity.level().isClientSide()) return;

        var callbacks = AbilityUtil.getInstances(entity, KryptoniteAbilitySerializers.ACTION_ON_CALLBACK.get());
        callbacks.forEach(ab -> ab.getAbility().onLoad(entity));

        if (kryptonite$initializedPowers && !kryptonite$previousPowers.contains(instance.getPowerId())) {
            callbacks.forEach(ab -> ab.getAbility().onGain(entity));
        }
    }

    @Inject(method = "removePowerInstance", at = @At("HEAD"), remap = false)
    private void kryptonite$onPowerRemoved(Holder<Power> power, CallbackInfo ci) {
        LivingEntity entity = ((EntityPowerHandler)(Object)this).getEntity();
        if (entity.level().isClientSide()) return;

        var powerId = power.unwrapKey().orElseThrow().identifier();

        if (((EntityPowerHandler)(Object)this).getPowerInstance(powerId) == null) {
            return;
        }

        for (var ability : power.value().getAbilities().values()) {
            if (ability instanceof ActionOnCallbackAbility callback) {
                kryptonite$removedCallbacks.add(callback);
                callback.onUnload(entity);
            }
        }
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void kryptonite$onCopyFrom(PalladiumEntityData<?, ?> source, CallbackInfo ci) {
        kryptonite$pendingRespawn = true;
    }

}