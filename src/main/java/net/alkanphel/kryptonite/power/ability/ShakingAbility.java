package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ShakingAbility extends Ability {

    public static final MapCodec<ShakingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("frequency", new StaticValue(3.25D)).forGetter(ab -> ab.frequency),
            Value.CODEC.optionalFieldOf("amplitude", new StaticValue(0.4D)).forGetter(ab -> ab.amplitude),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ShakingAbility::new));

    public final Value frequency;
    public final Value amplitude;

    public ShakingAbility(Value frequency, Value amplitude, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.frequency = frequency;
        this.amplitude = amplitude;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.SHAKING.get();
    }

    public static class Serializer extends AbilitySerializer<ShakingAbility> {

        @Override
        public MapCodec<ShakingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ShakingAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Makes you shake like a cold strider, curing zombie villager, overworld piglin, etc. Higher values take priority if multiple of this ability type are enabled.")
                    .addOptional("frequency", TYPE_VALUE, "Speed of the shaking.", 3.25D)
                    .addOptional("amplitude", TYPE_VALUE, "Strength of the shaking.", 1.0D)
                    .addExampleObject(new ShakingAbility(new StaticValue(3.25D), new StaticValue(0.4D), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}