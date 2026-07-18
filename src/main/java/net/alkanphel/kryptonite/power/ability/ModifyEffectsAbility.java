package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class ModifyEffectsAbility extends Ability {

    public static final MapCodec<ModifyEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Mode.CODEC.fieldOf("mode").forGetter(a -> a.mode),
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("modifiers", List.of()).forGetter(a -> a.modifiers),
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).optionalFieldOf("effects").forGetter(a -> a.effects),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyEffectsAbility::new));

    public final Mode mode;
    public final List<KryptoniteModifiers.ValueModifier> modifiers;
    public final Optional<HolderSet<MobEffect>> effects;

    public ModifyEffectsAbility(Mode mode, List<KryptoniteModifiers.ValueModifier> modifiers, Optional<HolderSet<MobEffect>> effects, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.mode = mode;
        this.modifiers = modifiers;
        this.effects = effects;
    }

    public boolean doesApply(Holder<MobEffect> effect) {
        return effects.map(set -> set.contains(effect)).orElse(true);
    }

    public int applyModifiers(int amplifier, LivingEntity entity, AbilityInstance<?> instance) {
        return Math.max(0, KryptoniteModifiers.applyModifiers(amplifier, modifiers, DataContext.forAbility(entity, instance)));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_EFFECTS.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyEffectsAbility> {

        @Override
        public MapCodec<ModifyEffectsAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyEffectsAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Effects")
                    .setDescription("Modifies the amplifier or duration for effects applied to the entity that has this ability.")
                    .add("mode", SettingType.enumList(Mode.values()), "Mode to use for the ability.")
                    .add("modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers to apply for the effects.")
                    .addOptional("effects", TYPE_MOB_EFFECT_TYPE_HOLDER_SET, "If specified, only modifies the listed effects upon them being added. If none specified, applies to all effects.")
                    .addExampleObject(new ModifyEffectsAbility(Mode.AMPLIFIER, List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(1.0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyEffectsAbility(Mode.DURATION, List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0.5), KryptoniteModifiers.Operation.MULTIPLY_TOTAL_ADDITIVE)), Optional.empty(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));

        }
    }

    public enum Mode implements StringRepresentable {
        AMPLIFIER, DURATION;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}