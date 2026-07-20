package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.Optional;

public record HasEffectCondition(Optional<PalladiumHolderSet<MobEffect>> mobEffect, Optional<Value> minDuration, Optional<Value> maxDuration, Optional<Value> minAmplifier, Optional<Value> maxAmplifier, Optional<Value> hiddenParticles) implements Condition {

    public static final MapCodec<HasEffectCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            PalladiumHolderSet.codec(Registries.MOB_EFFECT).optionalFieldOf("effect").forGetter(HasEffectCondition::mobEffect),
            Value.CODEC.optionalFieldOf("min_duration").forGetter(HasEffectCondition::minDuration),
            Value.CODEC.optionalFieldOf("max_duration").forGetter(HasEffectCondition::maxDuration),
            Value.CODEC.optionalFieldOf("min_amplifier").forGetter(HasEffectCondition::minAmplifier),
            Value.CODEC.optionalFieldOf("max_amplifier").forGetter(HasEffectCondition::maxAmplifier),
            Value.CODEC.optionalFieldOf("hidden_particles").forGetter(HasEffectCondition::hiddenParticles)
    ).apply(instance, HasEffectCondition::new));

    @Override
    public boolean test(DataContext context) {
        var entity = context.getLivingEntity();
        if (entity == null) return false;

        if (mobEffect.isEmpty()) {
            for (MobEffectInstance effectInstance : entity.getActiveEffects()) {
                if (matches(effectInstance, context)) {
                    return true;
                }
            }

            return false;
        }

        for (Holder<MobEffect> effectHolder : mobEffect.get().resolve(entity.registryAccess())) {
            MobEffectInstance effectInstance = entity.getEffect(effectHolder);

            if (effectInstance != null && matches(effectInstance, context)) {
                return true;
            }
        }

        return false;
    }

    private boolean matches(MobEffectInstance instance, DataContext context) {
        int duration = instance.getDuration();
        int amplifier = instance.getAmplifier();

        boolean durationCheck =
                minDuration.map(v -> duration >= v.getAsInt(context)).orElse(true)
                && maxDuration.map(v -> duration <= v.getAsInt(context)).orElse(true);

        boolean amplifierCheck =
                minAmplifier.map(v -> amplifier >= v.getAsInt(context)).orElse(true)
                && maxAmplifier.map(v -> amplifier <= v.getAsInt(context)).orElse(true);

        boolean particlesCheck = hiddenParticles
                .map(v -> instance.isVisible() == v.getAsBoolean(context))
                .orElse(true);

        return durationCheck && amplifierCheck && particlesCheck;
    }

    @Override
    public ConditionSerializer<HasEffectCondition> getSerializer() {
        return KryptoniteConditionSerializers.HAS_EFFECT.get();
    }

    public static class Serializer extends ConditionSerializer<HasEffectCondition> {

        @Override
        public MapCodec<HasEffectCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, HasEffectCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Has Effect")
                    .setDescription("Checks if the entity has the specified (potion) effect(s). Omitted fields will be ignored.")
                    .add("effect", TYPE_MOB_EFFECT_TYPE_HOLDER_SET, "IDs or tags of the required mob/potion effect")
                    .addOptional("min_duration", TYPE_VALUE, "Minimum duration in ticks the effect should have left.")
                    .addOptional("max_duration", TYPE_VALUE, "Maximum duration in ticks the effect should have left.")
                    .addOptional("min_amplifier", TYPE_VALUE, "Minimum amplifier the effect should have.")
                    .addOptional("max_amplifier", TYPE_VALUE, "Maximum amplifier the effect should have.")
                    .addOptional("hidden_particles", TYPE_VALUE, "If true, the effect must have particles hidden.")
                    .addExampleObject(new HasEffectCondition(Optional.of(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("slowness")))))), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()))
                    .addExampleObject(new HasEffectCondition(Optional.of(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("slowness"))), provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("mining_fatigue")))))), Optional.of(new StaticValue(30)), Optional.empty(), Optional.of(new StaticValue(1)), Optional.of(new StaticValue(3)), Optional.of(new StaticValue(true))));
        }
    }

}