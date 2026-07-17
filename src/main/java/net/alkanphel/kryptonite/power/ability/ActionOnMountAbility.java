package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
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
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnMountAbility extends Ability {

    public static final MapCodec<ActionOnMountAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            Codec.BOOL.optionalFieldOf("switch_to_dismount", false).forGetter(a -> a.switchToDismount),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnMountAbility::new));

    public final List<Action> entityActions;
    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;
    public final boolean switchToDismount;

    public ActionOnMountAbility(List<Action> entityActions, List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, boolean switchToDismount, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
        this.switchToDismount = switchToDismount;
    }

    public boolean doesApply(LivingEntity holder, Entity vehicle) {
        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, vehicle)) {
            return false;
        }

        return true;
    }

    public void runActions(LivingEntity holder, Entity vehicle) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, holder, vehicle);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_MOUNT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnMountAbility> {

        @Override
        public MapCodec<ActionOnMountAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnMountAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Mount")
                    .setDescription("Runs actions when the entity mounts (or dismounts) a vehicle. In the context of this ability, the \"actor\" is the ability holder & \"target\" mounted vehicle.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity that mounted the vehicle.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on either or both \"actor\" & \"target\" entities.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the actions will only be run if these bi conditions are fulfilled by either or both the \"actor\" & \"target\" entities.")
                    .addOptional("switch_to_dismount", TYPE_BOOLEAN, "If true, this ability will instead run when the entity dismounts a vehicle.", false)
                    .addExampleObject(new ActionOnMountAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on mount!"))), List.of(), List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("oak_boat")))))))), false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnMountAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on dismount!"))), List.of(), List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("oak_boat")))))))), true, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}