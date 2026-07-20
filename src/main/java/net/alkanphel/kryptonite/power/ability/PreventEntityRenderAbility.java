package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.client.render.OpacityRenderChanging;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public class PreventEntityRenderAbility extends Ability implements OpacityRenderChanging<PreventEntityRenderAbility> {

    private final Map<Entity, AnimationTimer> ANIMATION_TIMER_CONDITIONED = new WeakHashMap<>();

    public static final MapCodec<PreventEntityRenderAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.CODEC.optionalFieldOf("target_opacity", new StaticValue(0.0F)).forGetter(a -> a.targetOpacity),
            Condition.CODEC.optionalFieldOf("entity_conditions").forGetter(ab -> ab.entityConditions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEntityRenderAbility::new));

    public final Value targetOpacity;
    public final Optional<Condition> entityConditions;
    private final List<BiCondition> biEntityConditions;

    public PreventEntityRenderAbility(Value targetOpacity, Optional<Condition> entityConditions, List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.targetOpacity = targetOpacity;
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

    @Override
    public float getRenderOpacity(LivingEntity viewer, Entity target, AbilityInstance<PreventEntityRenderAbility> instance, float partialTick) {
        boolean applies = doesApply(viewer, target);

        AnimationTimerSetting timerSettings = this.getProperties().getAnimationTimerSetting();

        float toggleProgress = instance.getAnimationTimerProgressEased(partialTick);
        float conditionProgress;

        if (timerSettings != null) {
            AnimationTimer timer = ANIMATION_TIMER_CONDITIONED.computeIfAbsent(target, t -> new AnimationTimer(timerSettings, applies ? timerSettings.max() : timerSettings.min()));
            conditionProgress = timer.eased(partialTick);
        } else {
            conditionProgress = applies ? 1F : 0F;
        }

        float combinedProgress = Math.min(toggleProgress, conditionProgress);
        float targetOpacityValue = this.targetOpacity.getAsFloat(DataContext.forAbility(viewer, instance));

        return Mth.lerp(combinedProgress, 1F, targetOpacityValue);
    }

    public void onClientTick(LivingEntity viewer) {
        AnimationTimerSetting setting = this.getProperties().getAnimationTimerSetting();
        if (setting == null) return;

        ANIMATION_TIMER_CONDITIONED.forEach((target, timer) -> timer.tickAndUpdate(doesApply(viewer, target)));
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
                    .setDescription("Prevents entities from rendering and/or changing their opacity for the player that has this ability. Warning that these condition fields evaluate client-side. In the context of this ability, the \"actor\" is the player/viewer that has the ability & the \"target\" is the entities whose rendering have been modified.")
                    .addOptional("target_opacity", TYPE_VALUE, "The opacity value that the entities will render with.", 0)
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "If specified, only entities that fulfill these conditions will be affected.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, these conditions must be fulfilled by either or both \"actor\" & \"target\" entities.")
                    .addExampleObject(new PreventEntityRenderAbility(new StaticValue(0), Optional.empty(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntityRenderAbility(new StaticValue(0), Optional.empty(), List.of(), new AbilityProperties().animationTimer(new AnimationTimerSetting(0, 10, AnimationTimerSetting.Behaviour.FOLLOW, Easing.INEXPO)), AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntityRenderAbility(new StaticValue(0), Optional.empty(), List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("creeper")))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}