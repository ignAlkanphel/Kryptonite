package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventEffectsAbility extends Ability {

    public static final MapCodec<PreventEffectsAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("inverted", new StaticValue(false)).forGetter(a -> a.inverted),
            RegistryCodecs.homogeneousList(Registries.MOB_EFFECT).fieldOf("effects").forGetter(ab -> ab.effects),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEffectsAbility::new));

    public final Value inverted;
    public final HolderSet<MobEffect> effects;

    public PreventEffectsAbility(Value inverted, HolderSet<MobEffect> effects, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.inverted = inverted;
        this.effects = effects;
    }

    public static boolean isImmuneTo(LivingEntity entity, AbilityInstance<PreventEffectsAbility> ability, Holder<MobEffect> effect) {
        return ability.isEnabled() && ability.getAbility().inverted.getAsBoolean(DataContext.forAbility(entity, ability)) != ability.getAbility().effects.contains(effect);
    }

    @Override
    public AbilitySerializer<PreventEffectsAbility> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_EFFECTS.get();
    }

    public static class Serializer extends AbilitySerializer<PreventEffectsAbility> {

        @Override
        public MapCodec<PreventEffectsAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventEffectsAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the entity from being able to gain OR only be able to gain the specified effects. Does NOT clear effects.")
                    .addOptional("inverted", TYPE_VALUE, "If to be immune to all BUT the specified effects.", false)
                    .add("effects", TYPE_MOB_EFFECT_TYPE_HOLDER_SET, "The effects that you will be immune to.")
                    .addExampleObject(new PreventEffectsAbility(new StaticValue(false), HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("poison"))), provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("weakness")))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}