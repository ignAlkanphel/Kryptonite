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

import java.util.Collections;
import java.util.List;

public class FreezeAbility extends Ability {

    public static final MapCodec<FreezeAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, FreezeAbility::new));

    public FreezeAbility(AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.FREEZE.get();
    }

    public static class Serializer extends AbilitySerializer<FreezeAbility> {

        @Override
        public MapCodec<FreezeAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, FreezeAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Makes the entity that has this ability freeze as if in powdered snow.")
                    .addExampleObject(new FreezeAbility(AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()));
        }
    }

}