package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.ActorActionBiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class PreventCriticalHitAbility extends Ability {

    public static final MapCodec<PreventCriticalHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ApplyTo.CODEC.fieldOf("apply_to").forGetter(a -> a.applyTo),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventCriticalHitAbility::new));

    public final ApplyTo applyTo;
    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;

    public PreventCriticalHitAbility(ApplyTo applyTo, List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.applyTo = applyTo;
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesApply(Player attacker, Entity target) {
        return biEntityConditions.isEmpty() || BiCondition.checkConditions(biEntityConditions, attacker, target);
    }

    public void runActions(Player attacker, Entity target) {
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, attacker, target);
    }

    public static boolean shouldPreventCrit(Player attacker, Entity target) {
        boolean prevented = false;

        for (AbilityInstance<PreventCriticalHitAbility> instance : AbilityUtil.getEnabledInstances(attacker, KryptoniteAbilitySerializers.PREVENT_CRITICAL_HIT.get())) {
            var ability = instance.getAbility();

            if (ability.applyTo == ApplyTo.SELF && ability.doesApply(attacker, target)) {
                prevented = true;
                ability.runActions(attacker, target);
            }
        }

        if (target instanceof LivingEntity livingTarget) {
            for (AbilityInstance<PreventCriticalHitAbility> instance : AbilityUtil.getEnabledInstances(livingTarget, KryptoniteAbilitySerializers.PREVENT_CRITICAL_HIT.get())) {
                var ability = instance.getAbility();

                if (ability.applyTo == ApplyTo.OTHER && ability.doesApply(attacker, target)) {
                    prevented = true;
                    ability.runActions(attacker, target);
                }
            }
        }

        return prevented;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_CRITICAL_HIT.get();
    }

    public static class Serializer extends AbilitySerializer<PreventCriticalHitAbility> {

        @Override
        public MapCodec<PreventCriticalHitAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventCriticalHitAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents critical hits being dealt. In the context of this ability, the \"actor\" is the entity that dealt the critical hit & \"target\" the entity that was hit.")
                    .add("apply_to", SettingType.enumList(ApplyTo.values()), "If to prevent critical hits when attacking (\"self\") or when being attacked (\"other\").")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these actions will be run on either or both the \"actor\" & \"target\" entities upon preventing critical hits.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the ability will only prevent critical hits if these conditions are fulfilled by either or both the \"actor\" & the \"target\" entities.")
                    .addExampleObject(new PreventCriticalHitAbility(ApplyTo.SELF, List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()))
                    .addExampleObject(new PreventCriticalHitAbility(ApplyTo.SELF, List.of(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands("title @s actionbar {\"text\": \"Cannot deal critical hits!\", \"color\": \"red\"}"))))), List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("villager")))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()));
        }
    }

    public enum ApplyTo implements StringRepresentable {
        SELF, OTHER;

        public static final Codec<ApplyTo> CODEC = StringRepresentable.fromEnum(ApplyTo::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}