package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

// TODO More functionality
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

    public boolean prevents(EventParticle particle) {
        return eventParticles.contains(particle);
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
                    .addOptional("event_particles", SettingType.enumList(EventParticle.values()), "The particle events to prevent the spawning of.")
                    .addExampleObject(new PreventParticlesAbility(List.of(EventParticle.DEATH), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventParticlesAbility(List.of(EventParticle.SPRINTING, EventParticle.LANDING), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum EventParticle implements StringRepresentable {
        DEATH("death"),
        LANDING("landing"),
        SPRINTING("sprinting"),
        DROWNING("drowning"),
        SWEEP_ATTACK("sweep_attack"),
        CRITICAL_HIT("critical_hit"),
        MAGIC_CRITICAL_HIT("magical_critical_hit"),
        DAMAGE_INDICATOR("damage_indicator");

        public static final Codec<EventParticle> CODEC = StringRepresentable.fromEnum(EventParticle::values);
        private final String name;

        EventParticle(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

}