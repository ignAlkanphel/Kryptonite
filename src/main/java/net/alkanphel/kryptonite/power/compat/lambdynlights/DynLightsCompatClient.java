package net.alkanphel.kryptonite.power.compat.lambdynlights;

public class DynLightsCompatClient {
    public static void init() {
        DynLightsCompat.ABILITY_HANDLER = new DynLightsAbilityHandlerClient();
    }
}