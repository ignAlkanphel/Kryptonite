package net.alkanphel.kryptonite.client.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class MxyDustParticleType extends ParticleType<MxyDustParticleOptions> {
    public MxyDustParticleType() {
        super(false);
    }

    @Override
    public MapCodec<MxyDustParticleOptions> codec() {
        return MxyDustParticleOptions.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, MxyDustParticleOptions> streamCodec() {
        return MxyDustParticleOptions.STREAM_CODEC;
    }

}