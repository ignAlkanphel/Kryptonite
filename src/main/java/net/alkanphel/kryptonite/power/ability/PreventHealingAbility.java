package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventHealingAbility extends Ability {

    public static final MapCodec<PreventHealingAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("full_prevention", new StaticValue(false)).forGetter(a -> a.fullPrevention),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventHealingAbility::new));

    public final Value fullPrevention;

    public PreventHealingAbility(Value fullPrevention, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.fullPrevention = fullPrevention;
    }

    public static boolean isFullPrevention(LivingEntity entity, AbilityInstance<PreventHealingAbility> ability) {
        return ability.isEnabled() && ability.getAbility().fullPrevention.getAsBoolean(DataContext.forAbility(entity, ability));
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
                    .addOptional("full_prevention", TYPE_VALUE, "If true, healing from things like potion effects will also be prevented.", false)
                    .addExampleObject(new PreventHealingAbility(new StaticValue(false), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventHealingAbility(new StaticValue(true), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}