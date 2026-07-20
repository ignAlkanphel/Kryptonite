package net.alkanphel.kryptonite.client.render;

public interface KryptoniteLivingEntityRenderState {
    void kryptonite$setDamageTint(int color);
    int kryptonite$getDamageTint();

    void kryptonite$setDamageTintAlpha(float alpha);
    float kryptonite$getDamageTintAlpha();

    void kryptonite$setShakingFrequency(float frequency);
    float kryptonite$getShakingFrequency();

    void kryptonite$setShakingAmplitude(float amplitude);
    float kryptonite$getShakingAmplitude();
}