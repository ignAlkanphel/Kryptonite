package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventGlidingAbility extends Ability {

    public static final MapCodec<PreventGlidingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventGlidingAbility::new));

    public PreventGlidingAbility(AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_GLIDING.get();
    }

    public static class Serializer extends AbilitySerializer<PreventGlidingAbility> {

        @Override
        public MapCodec<PreventGlidingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventGlidingAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents & stops the player from gliding with an elytra.")
                    .addExampleObject(new PreventGlidingAbility(AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}