package net.alkanphel.kryptonite.power.compat.shouldersurfing;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ShoulderSurfingAbility;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;

public class ShoulderSurfingCompat {
    public static void init() {}

    public static boolean isTypeEnabled(Player player) {
        return AbilityUtil.isTypeEnabled(player, KryptoniteAbilitySerializers.SHOULDER_SURFING.get());
    }

    public static boolean isCameraCoupled(Player player) {
        for (AbilityInstance<ShoulderSurfingAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.SHOULDER_SURFING.get())) {
            if (instance.getAbility().cameraCoupling.get(DataContext.forAbility(player, instance))) {
                return true;
            }
        }

        return false;
    }

}