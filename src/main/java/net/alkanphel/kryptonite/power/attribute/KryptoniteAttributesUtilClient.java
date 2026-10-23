package net.alkanphel.kryptonite.power.attribute;

import net.alkanphel.kryptonite.client.render.KryptoniteLivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class KryptoniteAttributesUtilClient {

    public static boolean inGui = false;

    public static double getModelDepth(LivingEntityRenderState state, double original) {
        if (inGui) return original;
        return Mth.clamp(original * ((KryptoniteLivingEntityRenderState) state).kryptonite$getModelDepth(), KryptoniteAttributesUtil.SCALE_MIN, KryptoniteAttributesUtil.SCALE_MAX);
    }

}