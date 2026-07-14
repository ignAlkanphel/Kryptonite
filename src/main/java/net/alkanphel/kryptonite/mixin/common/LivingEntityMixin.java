package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.network.p2c.S2CSyncAttacker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

}
