package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.IsUnderWaterCondition;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventEntitySelectionAbility extends Ability {

    public static final MapCodec<PreventEntitySelectionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(ab -> ab.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEntitySelectionAbility::new));

    public final List<BiCondition> biEntityConditions;

    public PreventEntitySelectionAbility(List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesPrevent(LivingEntity holder, Entity target) {
        return biEntityConditions.isEmpty() || BiCondition.checkConditions(biEntityConditions, holder, target);
    }

    public static boolean shouldPreventSelection(LivingEntity viewer, Entity target) {
        return AbilityUtil.getEnabledInstances(viewer, KryptoniteAbilitySerializers.PREVENT_ENTITY_SELECTION.get())
                .stream().anyMatch(instance -> instance.getAbility().doesPrevent(viewer, target));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_ENTITY_SELECTION.get();
    }

    public static class Serializer extends AbilitySerializer<PreventEntitySelectionAbility> {

        @Override
        public MapCodec<PreventEntitySelectionAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventEntitySelectionAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents entities from being selected/targeted by the crosshair. In the context of this ability, the \"actor\" is the ability holder & \"target\" the selected entity.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only prevents entity selection if these conditions are fulfilled.")
                    .addExampleObject(new PreventEntitySelectionAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntitySelectionAbility(List.of(new TargetConditionBiCondition(new IsUnderWaterCondition())), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}