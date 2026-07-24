package net.alkanphel.kryptonite.power.compat.shouldersurfing;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityUtil;

public class ShoulderSurfingCompat {
    public static void init() {}

    public static boolean isTypeEnabled(Player player) {
        return AbilityUtil.isTypeEnabled(player, KryptoniteAbilitySerializers.SHOULDER_SURFING.get());
    }

    public static boolean isCameraCoupled(Player player) {
        return AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.SHOULDER_SURFING.get())
                .stream().anyMatch(instance -> instance.getAbility().cameraCoupling.getAsBoolean(DataContext.forAbility(player, instance)));
    }

}