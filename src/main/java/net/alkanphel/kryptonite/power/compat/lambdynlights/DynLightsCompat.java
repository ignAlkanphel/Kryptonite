package net.alkanphel.kryptonite.power.compat.lambdynlights;

import net.neoforged.bus.api.IEventBus;

public class DynLightsCompat {
    public static boolean ENABLED = false;

    public static DynLightsAbilityHandler ABILITY_HANDLER = new DynLightsAbilityHandler();

    public static void init(IEventBus eventBus) {
        ENABLED = true;
    }

}