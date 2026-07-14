package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.util.KryptoniteTags;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.Power;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.dampening.PowerDampeningSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = PowerInstance.class, priority = 1100)
public class PowerInstanceMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/threetag/palladium/power/dampening/PowerDampeningSource;isDampened(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/LivingEntity;)Z"))
    private boolean kryptonite$ignoreDampening(Holder<Power> power, LivingEntity entity) {
        if (power.is(KryptoniteTags.Powers.IGNORES_DAMPENING)) {
            return false;
        }

        return PowerDampeningSource.isDampened(power, entity);
    }

}