package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class PreventCriticalHitAbility extends Ability {

    public static final MapCodec<PreventCriticalHitAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ApplyTo.CODEC.fieldOf("apply_to").forGetter(a -> a.applyTo),
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventCriticalHitAbility::new));

    public final ApplyTo applyTo;
    public final List<Action> entityActions;
    public final Condition entityConditions;

    public PreventCriticalHitAbility(ApplyTo applyTo, List<Action> entityActions, Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.applyTo = applyTo;
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
    }

    public boolean doesApply(Player source, Entity target, AbilityInstance<?> abilityInstance) {
        return entityConditions.test(DataContext.forAffected(target, source, abilityInstance));
    }

    public void runActions(Player source, Entity target, AbilityInstance<?> abilityInstance) {
        Action.runList(entityActions, DataContext.forAffected(source, target, abilityInstance));
    }

    public static boolean shouldPreventCrit(Player attacker, Entity target) {
        boolean prevented = false;

        for (AbilityInstance<PreventCriticalHitAbility> instance : AbilityUtil.getEnabledInstances(attacker, KryptoniteAbilitySerializers.PREVENT_CRITICAL_HIT.get())) {
            var ability = instance.getAbility();

            if (ability.applyTo == ApplyTo.SELF && ability.doesApply(attacker, target, instance)) {
                prevented = true;
                ability.runActions(attacker, target, instance);
            }
        }

        if (target instanceof LivingEntity livingTarget) {
            for (AbilityInstance<PreventCriticalHitAbility> instance : AbilityUtil.getEnabledInstances(livingTarget, KryptoniteAbilitySerializers.PREVENT_CRITICAL_HIT.get())) {
                var ability = instance.getAbility();

                if (ability.applyTo == ApplyTo.OTHER && ability.doesApply(attacker, target, instance)) {
                    prevented = true;
                    ability.runActions(attacker, target, instance);
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
            builder.setDescription("Prevents critical hits from being dealt. For this ability, the \"source\" is the attacking player & the \"target\" is the entity being hit.")
                    .add("apply_to", SettingType.enumList(ApplyTo.values()), "Whether to prevent critical hits when attacking (\"self\") or when being attacked (\"other\").")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the source when a critical hit is prevented.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet to prevent critical hits.")
                    .addExampleObject(new PreventCriticalHitAbility(ApplyTo.SELF, List.of(), TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum ApplyTo implements StringRepresentable {
        SELF, OTHER;

        public static final Codec<ApplyTo> CODEC = StringRepresentable.fromEnum(ApplyTo::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}