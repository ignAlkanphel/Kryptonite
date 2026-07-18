package net.alkanphel.kryptonite.client.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ScalableParticleOptionsBase;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import org.joml.Vector3f;

public class MxyDustParticleOptions extends ScalableParticleOptionsBase {

    public static final MapCodec<MxyDustParticleOptions> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ExtraCodecs.ARGB_COLOR_CODEC.fieldOf("color").forGetter(p -> p.color),
            SCALE.fieldOf("scale").forGetter(ScalableParticleOptionsBase::getScale),
            Codec.BOOL.optionalFieldOf("glow", false).forGetter(p -> p.glow)
    ).apply(inst, MxyDustParticleOptions::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MxyDustParticleOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, buf -> buf.color, ByteBufCodecs.FLOAT, ScalableParticleOptionsBase::getScale,
            ByteBufCodecs.BOOL, p -> p.glow,
            MxyDustParticleOptions::new
    );

    private final int color;
    private final boolean glow;

    public MxyDustParticleOptions(int color, float scale, boolean glow) {
        super(scale);
        this.color = color;
        this.glow = glow;
    }

    @Override
    public ParticleType<MxyDustParticleOptions> getType() {
        return KryptoniteParticles.MXY_DUST.get();
    }

    public Vector3f getColor() {
        return ARGB.vector3fFromRGB24(this.color);
    }

    public float getAlpha() {
        return ARGB.alpha(this.color) / 255.0F;
    }

    public boolean isGlowing() {
        return this.glow;
    }

}