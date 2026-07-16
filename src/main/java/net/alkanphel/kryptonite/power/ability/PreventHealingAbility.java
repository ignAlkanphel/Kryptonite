package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventHealingAbility extends Ability {

    public static final MapCodec<PreventHealingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("full_prevention", false).forGetter(ab -> ab.fullPrevention),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventHealingAbility::new));

    public final boolean fullPrevention;

    public PreventHealingAbility(boolean fullPrevention, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.fullPrevention = fullPrevention;
    }

    public static boolean isFullPrevention(AbilityInstance<PreventHealingAbility> ability) {
        return ability.isEnabled() && ability.getAbility().fullPrevention;
    }

    @Override
    public AbilitySerializer<PreventHealingAbility> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_HEALING.get();
    }

    public static class Serializer extends AbilitySerializer<PreventHealingAbility> {

        @Override
        public MapCodec<PreventHealingAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventHealingAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the entity from healing via natural means (e.g. the \"minecraft:natural_health_regeneration\" game rule).")
                    .addOptional("full_prevention", TYPE_BOOLEAN, "If true, healing from things like potion effects will also be prevented.", false)
                    .addExampleObject(new PreventHealingAbility(false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventHealingAbility(true, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}