package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
import net.alkanphel.kryptonite.power.logic.operation.ItemOperation;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.HasEffectCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnItemPickupAbility extends Ability implements Prioritized {

    public static final MapCodec<ActionOnItemPickupAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("target_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            ItemOperation.CODEC.optionalFieldOf("item_operations", ItemOperation.EMPTY).forGetter(a -> a.itemOperation),
            ItemFilter.CODEC.optionalFieldOf("item_filters", ItemFilter.EMPTY).forGetter(a -> a.itemFilter),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemPickupAbility::new));

    private final List<Action> entityActions;
    private final Condition entityConditions;
    private final ItemOperation itemOperation;
    private final ItemFilter itemFilter;
    private final int priority;

    public ActionOnItemPickupAbility(List<Action> entityActions, Condition entityConditions, ItemOperation itemOperation, ItemFilter itemFilter, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
        this.itemOperation = itemOperation;
        this.itemFilter = itemFilter;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public boolean doesApply(ItemStack stack, Entity thrower, Entity holder) {
        if (!itemFilter.test(holder.level(), stack)) {
            return false;
        }

        return entityConditions.test(DataContext.forAffected(holder, thrower, null));
    }

    public void runEntityActions(Entity thrower, Entity holder) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forAffected(holder, thrower, null));
    }

    public void runItemOperations(Entity holder, SlotAccess stackReference) {
        if (!itemOperation.operations().isEmpty()) itemOperation.run(holder.level(), stackReference);
    }

    public static void runEntityActions(CallInstance<ActionOnItemPickupAbility> callInstance, Entity throwerEntity, Entity holderEntity) {
        callInstance.forEachByPriority(ability -> ability.runEntityActions(throwerEntity, holderEntity));
    }

    public static CallInstance<ActionOnItemPickupAbility> runItemOperations(Entity throwerEntity, SlotAccess stackReference, Entity holderEntity) {
        if (!(holderEntity instanceof LivingEntity livingEntity)) {
            return new CallInstance<>();
        }

        CallInstance<ActionOnItemPickupAbility> callInstance = new CallInstance<>();
        callInstance.add(livingEntity, ActionOnItemPickupAbility.class, ability -> ability.doesApply(stackReference.get(), throwerEntity, holderEntity));

        callInstance.forEachByPriority(ability -> ability.runItemOperations(holderEntity, stackReference));

        return callInstance;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_ITEM_PICKUP.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnItemPickupAbility> {

        @Override
        public MapCodec<ActionOnItemPickupAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnItemPickupAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when an entity picks up an item. For this ability, the \"source\" is the entity that threw the item & the \"target\" is the entity picking up the item.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the entity picking up the item.")
                    .addOptional("target_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet for the actions to perform.")
                    .addOptional("item_operations", ItemOperation.documentation(provider), "Item operation(s) performed on the item stack being picked up.")
                    .addOptional("item_filters", ItemFilter.documentation(provider), "Item filter(s) the item being picked up must pass for actions to perform.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnItemPickupAbility(List.of(new RunCommandAction(new ParsedCommands("say AHHH!!! PICKED UP PICKED UP!!!"))), TrueCondition.INSTANCE, ItemOperation.EMPTY, ItemFilter.EMPTY, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemPickupAbility(List.of(new RunCommandAction(new ParsedCommands("say You got speed? Pick me up, baby!"))), new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("speed")))))), ItemOperation.EMPTY, ItemFilter.EMPTY, 5, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}