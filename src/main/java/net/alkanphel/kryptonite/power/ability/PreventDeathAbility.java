package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.condition.damage.AttackerDamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;
import java.util.Optional;

public class PreventDeathAbility extends Ability {

    public static final MapCodec<PreventDeathAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(ab -> ab.entityActions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(ab -> ab.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventDeathAbility::new));

    public final List<Action> entityActions;
    public final List<DamageCondition> damageConditions;

    public PreventDeathAbility(List<Action> entityActions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return DamageCondition.checkConditions(damageConditions, source, amount);
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    public static boolean doesPrevent(LivingEntity entity, DamageSource source, float amount) {
        boolean prevented = false;

        for (AbilityInstance<PreventDeathAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_DEATH.get())) {
            if (!instance.getAbility().doesApply(source, amount)) {
                continue;
            }

            instance.getAbility().runActions(entity);
            prevented = true;
        }

        return prevented;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_DEATH.get();
    }

    public static class Serializer extends AbilitySerializer<PreventDeathAbility> {

        @Override
        public MapCodec<PreventDeathAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventDeathAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents entity from dying. Damage that kills will instead reduce health to 1/2 a heart.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity when death is prevented.")
                    .addOptional("damage_conditions", TYPE_CONDITION_LIST, "If specified, death is only prevented when these damage condition are fulfilled.")
                    .addExampleObject(new PreventDeathAbility(List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventDeathAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say My death was greatly exaggerated!")))), List.of(new AttackerDamageCondition(Optional.of(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("iron_golem"))))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}