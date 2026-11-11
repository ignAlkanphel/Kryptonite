package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.client.render.OpacityRenderChanging;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
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
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.Easing;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class PreventEntityRenderAbility extends Ability implements OpacityRenderChanging<PreventEntityRenderAbility> {

    private final Map<Entity, AnimationTimer> ANIMATION_TIMER_CONDITIONED = new WeakHashMap<>();

    public static final MapCodec<PreventEntityRenderAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Value.NUMBER_CODEC.optionalFieldOf("target_opacity", new StaticValue(0.0F)).forGetter(a -> a.targetOpacity),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEntityRenderAbility::new));

    public final Value targetOpacity;
    public final Condition entityConditions;

    public PreventEntityRenderAbility(Value targetOpacity, Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.targetOpacity = targetOpacity;
        this.entityConditions = entityConditions;
    }

    public boolean doesApply(LivingEntity viewer, Entity target) {
        return entityConditions.test(DataContext.forAffected(target, viewer, null));
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
                    .setDescription("Prevents entities from rendering and/or changing their opacity for the player that has this ability. For this ability, the \"source\" is the player/viewer that has the ability & the \"target\" is the entity whose rendering is being modified.")
                    .addOptional("target_opacity", TYPE_FLOAT_VALUE, "The opacity value that the entities will render with. A value of 0 will explicitly cause entities to no longer render rather than have their opacity change.", 0)
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet to have its rendering modified. Use 'palladium:as_source' to check conditions against the viewer instead. Evaluates client-side.")
                    .addExampleObject(new PreventEntityRenderAbility(new StaticValue(0), TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntityRenderAbility(new StaticValue(0), TrueCondition.INSTANCE, new AbilityProperties().animationTimer(new AnimationTimerSetting(0, 10, AnimationTimerSetting.Behaviour.FOLLOW, Easing.INEXPO)), AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntityRenderAbility(new StaticValue(0), new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("creeper")))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}