package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.util.KryptoniteTags;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.dampening.PowerDampeningSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = PowerDampeningSource.class, priority = 1100)
public class PowerDampeningSourceMixin {

    @Inject(method = "collect", at = @At("RETURN"), cancellable = true)
    private static void kryptonite$ignoreDampening(LivingEntity entity, CallbackInfoReturnable<Predicate<Holder<Power>>> cir) {
        Predicate<Holder<Power>> dampening = cir.getReturnValue();
        cir.setReturnValue(power -> !power.is(KryptoniteTags.Powers.IGNORES_DAMPENING) && dampening.test(power));
    }

}