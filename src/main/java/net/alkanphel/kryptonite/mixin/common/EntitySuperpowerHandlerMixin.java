package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.util.KryptoniteTags;
import net.minecraft.core.Holder;
import net.threetag.palladium.config.PalladiumServerConfig;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.superpower.EntitySuperpowerHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(EntitySuperpowerHandler.class)
public class EntitySuperpowerHandlerMixin {

    @Shadow @Final
    private List<Holder<Power>> superpowers;

    @Inject(method = "canBeAdded", at = @At("HEAD"), cancellable = true)
    private void kryptonite$ignoreSuperpowerSetLimit(Holder<Power> powerHolder, CallbackInfoReturnable<Boolean> cir) {
        Power power = powerHolder.value();

        if (superpowers.contains(powerHolder)) {
            cir.setReturnValue(false);
            return;
        }

        if (power.getParentId() == null) {

            long count = superpowers
                    .stream()
                    .filter(holder -> holder.value().getParentId() == null)
                    .filter(holder -> !holder.is(KryptoniteTags.Powers.IGNORES_MAX_SUPERPOWER_SETS))
                    .count();

            if (powerHolder.is(KryptoniteTags.Powers.IGNORES_MAX_SUPERPOWER_SETS)) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(count < PalladiumServerConfig.MAX_SUPERPOWER_SETS.get());
            }
        }
    }

}