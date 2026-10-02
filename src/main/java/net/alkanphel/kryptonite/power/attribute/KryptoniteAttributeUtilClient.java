package net.alkanphel.kryptonite.power.attribute;

import net.alkanphel.kryptonite.client.render.KryptoniteLivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;

public class KryptoniteAttributeUtilClient {

    public static double getModelDepth(LivingEntityRenderState state, double original) {
        return Mth.clamp(original * ((KryptoniteLivingEntityRenderState) state).kryptonite$getModelDepth(), KryptoniteAttributeUtil.MIN_SCALE, KryptoniteAttributeUtil.MAX_SCALE);
    }

}