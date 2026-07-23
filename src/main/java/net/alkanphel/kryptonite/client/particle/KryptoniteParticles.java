package net.alkanphel.kryptonite.client.particle;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class KryptoniteParticles {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, Kryptonite.MOD_ID);

    public static final Supplier<ParticleType<MxyDustParticleOptions>> MXY_DUST = PARTICLE_TYPES.register("mxy_dust", MxyDustParticleType::new);

}