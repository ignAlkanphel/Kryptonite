package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;
import java.util.Optional;

public class PreventEntityRenderAbility extends Ability {

    public static final MapCodec<PreventEntityRenderAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("entity_conditions").forGetter(ab -> ab.entityConditions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEntityRenderAbility::new));

    public final Optional<Condition> entityConditions;
    private final List<BiCondition> biEntityConditions;

    public PreventEntityRenderAbility(Optional<Condition> entityConditions, List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityConditions = entityConditions;
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesApply(LivingEntity holder, Entity target) {
        if (entityConditions.isPresent() && !entityConditions.get().test(DataContext.forEntity(target))) {
            return false;
        }

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, target)) {
            return false;
        }

        return true;
    }

    public static boolean shouldPreventRender(LivingEntity viewer, Entity target) {
        return AbilityUtil.getEnabledInstances(viewer, KryptoniteAbilitySerializers.PREVENT_ENTITY_RENDER.get())
                .stream()
                .anyMatch(instance -> instance.getAbility().doesApply(viewer, target));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_ENTITY_RENDER.get();
    }

    public static class Serializer extends AbilitySerializer<PreventEntityRenderAbility> {

        @Override
        public MapCodec<PreventEntityRenderAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventEntityRenderAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Prevent Entity Render")
                    .setDescription("Prevents entities from rendering to the entity that has the ability. Warning that the condition fields of this ability evaluate client-side.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "If specified, only prevents rendering of entities fulfilling these conditions.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the ability will only be active if these bi conditions are fulfilled by either or both \"actor\" (ability holder) & \"target\" (entity that won't render) entities.")
                    .addExampleObject(new PreventEntityRenderAbility(Optional.empty(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntityRenderAbility(Optional.empty(), List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("creeper")))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}