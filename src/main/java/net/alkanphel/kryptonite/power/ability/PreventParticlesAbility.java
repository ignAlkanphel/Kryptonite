package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class PreventParticlesAbility extends Ability {

    public static final MapCodec<PreventParticlesAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ExtraCodecs.compactListCodec(EventParticle.CODEC).optionalFieldOf("event_particles", List.of()).forGetter(a -> a.eventParticles),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventParticlesAbility::new));

    public final List<EventParticle> eventParticles;

    public PreventParticlesAbility(List<EventParticle> eventParticles, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.eventParticles = eventParticles;
    }

    public static boolean prevents(LivingEntity entity, EventParticle particle) {
        for (AbilityInstance<PreventParticlesAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_PARTICLES.get())) {
            if (instance.getAbility().eventParticles.contains(particle)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_PARTICLES.get();
    }

    public static class Serializer extends AbilitySerializer<PreventParticlesAbility> {

        @Override
        public MapCodec<PreventParticlesAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventParticlesAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the entity that has this ability from spawning certain particles created during certain vanilla events.")
                    .addOptional("event_particles", SettingType.enumList(EventParticle.values()), "The particle events to prevent.")
                    .addExampleObject(new PreventParticlesAbility(List.of(EventParticle.DEATH), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventParticlesAbility(List.of(EventParticle.SPRINTING, EventParticle.LANDING), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum EventParticle implements StringRepresentable {
        DEATH,
        LANDING,
        SPRINTING,
        DROWNING,
        SWEEP_ATTACK,
        CRITICAL_HIT,
        MAGIC_CRITICAL_HIT,
        DAMAGE_INDICATOR;

        public static final Codec<EventParticle> CODEC = StringRepresentable.fromEnum(EventParticle::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}