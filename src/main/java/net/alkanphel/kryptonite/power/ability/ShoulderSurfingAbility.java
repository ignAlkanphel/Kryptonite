package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ShoulderSurfingAbility extends Ability {

    public static final MapCodec<ShoulderSurfingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BooleanValueHolder.CODEC.optionalFieldOf("camera_coupling", BooleanValueHolder.FALSE).forGetter(ab -> ab.cameraCoupling),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ShoulderSurfingAbility::new));

    public final BooleanValueHolder cameraCoupling;

    public ShoulderSurfingAbility(BooleanValueHolder cameraCoupling, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.cameraCoupling = cameraCoupling;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.SHOULDER_SURFING.get();
    }

    public static class Serializer extends AbilitySerializer<ShoulderSurfingAbility> {

        @Override
        public MapCodec<ShoulderSurfingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ShoulderSurfingAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Allows to force the \"Shoulder Surfing\" mod camera to be coupled. Does nothing without the mod installed.")
                    .addOptional("camera_coupling", TYPE_BOOLEAN_VALUE, "If true, the Shoulder Surfing camera will be forced to be coupled (camera locked to your facing direction like vanilla).", false)
                    .addExampleObject(new ShoulderSurfingAbility(BooleanValueHolder.FALSE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}