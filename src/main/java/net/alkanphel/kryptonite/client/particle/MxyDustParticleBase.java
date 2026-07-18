package net.alkanphel.kryptonite.client.particle;

import net.alkanphel.kryptonite.client.render.KryptoniteRenderTypes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.util.Mth;

public class MxyDustParticleBase<T extends ScalableParticleOptionsBase> extends SingleQuadParticle {
    private final SpriteSet sprites;
    private final boolean glow;

    public MxyDustParticleBase(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, T options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());
        this.friction = 0.96F;
        this.speedUpWhenYMotionIsBlocked = true;
        this.sprites = sprites;
        this.xd *= 0.1F;
        this.yd *= 0.1F;
        this.zd *= 0.1F;
        this.glow = (options instanceof MxyDustParticleOptions f) && f.isGlowing();
        this.quadSize *= 0.75F * options.getScale();
        int i = (int)((double)8.0F / (this.random.nextDouble() * 0.8 + 0.2));
        this.lifetime = (int)Math.max((float)i * options.getScale(), 1.0F);
        this.setSpriteFromAge(sprites);
    }

//    public static final Layer CUSTOM_LAYER = new Layer(true, TextureAtlas.LOCATION_PARTICLES, RenderPipelines.TRANSLUCENT_PARTICLE);
    public static final Layer ADDITIVE = new Layer(true, TextureAtlas.LOCATION_PARTICLES, KryptoniteRenderTypes.Pipelines.ADDITIVE_PARTICLE);

    public Layer getLayer() {
        return ADDITIVE;
    }

    public float getQuadSize(float p_172109_) {
        return this.quadSize * Mth.clamp(((float)this.age + p_172109_) / (float)this.lifetime * 32.0F, 0.0F, 1.0F);
    }

    @Override
    public int getLightCoords(float partialTick) {
        return this.glow ? 0xF000F0 : super.getLightCoords(partialTick);
    }

    public void tick() {
        super.tick();
        this.setSpriteFromAge(this.sprites);
    }

}