package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

}