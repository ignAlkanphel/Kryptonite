package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.util.KryptoniteTags;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbilityInstance.class, priority = 1100)
public class AbilityInstanceMixin {

    @Redirect(method = "tick(Lnet/minecraft/world/entity/LivingEntity;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isSpectator()Z"))
    private boolean kryptonite$allowSpectatorAbilities(LivingEntity entity) {
        AbilityInstance<?> instance = (AbilityInstance<?>) (Object) this;

        if (instance.getPowerInstance().getPower().is(KryptoniteTags.Powers.IGNORES_SPECTATOR_CHECK)) {
            return false;
        }

        return entity.isSpectator();
    }

}