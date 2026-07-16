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

public class ImmediateRespawnAbility extends Ability {

    public static final MapCodec<ImmediateRespawnAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ImmediateRespawnAbility::new));

    public ImmediateRespawnAbility(AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.IMMEDIATE_RESPAWN.get();
    }

    public static class Serializer extends AbilitySerializer<ImmediateRespawnAbility> {

        @Override
        public MapCodec<ImmediateRespawnAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ImmediateRespawnAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows the player to instantly respawn as if the vanilla \"immediate_respawn\" game rule was enabled.")
                    .addExampleObject(new ImmediateRespawnAbility(AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()));
        }
    }

}