package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.condition.UnderWaterCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventEntitySelectionAbility extends Ability {

    public static final MapCodec<PreventEntitySelectionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(ab -> ab.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEntitySelectionAbility::new));

    public final Condition entityConditions;

    public PreventEntitySelectionAbility(Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityConditions = entityConditions;
    }

    public boolean doesPrevent(LivingEntity viewer, Entity target, AbilityInstance<?> abilityInstance) {
        return entityConditions.test(DataContext.forAffected(target, viewer, abilityInstance));
    }

    public static boolean shouldPreventSelection(LivingEntity viewer, Entity target) {
        return AbilityUtil.getEnabledInstances(viewer, KryptoniteAbilitySerializers.PREVENT_ENTITY_SELECTION.get())
                .stream().anyMatch(instance -> instance.getAbility().doesPrevent(viewer, target, instance));
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
            builder.setDescription("Prevents entities from being selected/targeted by the crosshair. For this ability, the \"source\" is the player that has the ability & the \"target\" is the selected entity.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet to prevent entity selection.")
                    .addExampleObject(new PreventEntitySelectionAbility(TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntitySelectionAbility(new UnderWaterCondition(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}