package net.alkanphel.kryptonite.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

public class MxyDustParticle extends MxyDustParticleBase<MxyDustParticleOptions> {
    public MxyDustParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, MxyDustParticleOptions options, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, options, sprites);
        Vector3f color = options.getColor();
        this.rCol = color.x();
        this.gCol = color.y();
        this.bCol = color.z();
        float rawAlpha = options.getAlpha();
        this.alpha = Math.max(rawAlpha, 0.11F);
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<MxyDustParticleOptions> {

        @Override
        public Particle createParticle(MxyDustParticleOptions options, ClientLevel level, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
            return new MxyDustParticle(level, x, y, z, xd, yd, zd, options, this.sprites);
        }
    }

}