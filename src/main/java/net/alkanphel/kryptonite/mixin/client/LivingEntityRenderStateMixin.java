package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.client.render.KryptoniteLivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements KryptoniteLivingEntityRenderState {

    @Unique private int kryptonite$damageTint = -1;
    @Unique private float kryptonite$damageOverlayProgress = 0.0F;

    @Unique private float kryptonite$shakingFrequency = 0.0F;
    @Unique private float kryptonite$shakingAmplitude = 0.0F;

    @Override
    public void kryptonite$setDamageTint(int color) {
        this.kryptonite$damageTint = color;
    }

    @Override
    public int kryptonite$getDamageTint() {
        return this.kryptonite$damageTint;
    }

    @Override
    public void kryptonite$setDamageTintAlpha(float alpha) {
        this.kryptonite$damageOverlayProgress = alpha;
    }

    @Override
    public float kryptonite$getDamageTintAlpha() {
        return this.kryptonite$damageOverlayProgress;
    }

    @Override
    public void kryptonite$setShakingFrequency(float frequency) {
        this.kryptonite$shakingFrequency = frequency;
    }

    @Override
    public float kryptonite$getShakingFrequency() {
        return this.kryptonite$shakingFrequency;
    }

    @Override
    public void kryptonite$setShakingAmplitude(float amplitude) {
        this.kryptonite$shakingAmplitude = amplitude;
    }

    @Override
    public float kryptonite$getShakingAmplitude() {
        return this.kryptonite$shakingAmplitude;
    }

}