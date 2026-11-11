package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
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
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemPickupAbility::new));

    private final List<Action> entityActions;
    private final List<ItemAction> itemActions;
    private final Condition entityConditions;
    private final List<ItemCondition> itemConditions;
    private final int priority;

    public ActionOnItemPickupAbility(List<Action> entityActions, List<ItemAction> itemActions, Condition entityConditions, List<ItemCondition> itemConditions, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.itemActions = itemActions;
        this.entityConditions = entityConditions;
        this.itemConditions = itemConditions;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public boolean doesApply(ItemStack stack, Entity thrower, Entity holder) {
        if (!itemConditions.isEmpty() && !ItemCondition.checkConditions(itemConditions, holder.level(), stack)) {
            return false;
        }

        return entityConditions.test(DataContext.forAffected(holder, thrower, null));
    }

    public void runEntityActions(Entity thrower, Entity holder) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forAffected(holder, thrower, null));
    }

    public void runItemActions(Entity holder, SlotAccess stackReference) {
        if (!itemActions.isEmpty()) ItemAction.runList(itemActions, holder.level(), stackReference);
    }

    public static void runEntityActions(CallInstance<ActionOnItemPickupAbility> callInstance, Entity throwerEntity, Entity holderEntity) {
        callInstance.forEachByPriority(ability -> ability.runEntityActions(throwerEntity, holderEntity));
    }

    public static CallInstance<ActionOnItemPickupAbility> runItemActions(Entity throwerEntity, SlotAccess stackReference, Entity holderEntity) {
        if (!(holderEntity instanceof LivingEntity livingEntity)) {
            return new CallInstance<>();
        }

        CallInstance<ActionOnItemPickupAbility> callInstance = new CallInstance<>();
        callInstance.add(livingEntity, ActionOnItemPickupAbility.class, ability -> ability.doesApply(stackReference.get(), throwerEntity, holderEntity));

        callInstance.forEachByPriority(ability -> ability.runItemActions(holderEntity, stackReference));

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
            builder.setName("Action On Item Pickup")
                    .setDescription("Performs actions when an entity picks up an item. For this ability, the \"source\" is the entity that threw the item & the \"target\" is the entity picking up the item.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the entity picking up the item.")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Action(s) performed on the item stack being picked up.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet for the actions to perform.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Item condition(s) the item being picked up must meet for actions to perform.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnItemPickupAbility(List.of(new RunCommandAction(new ParsedCommands("say AHHH!!! PICKED UP PICKED UP!!!"))), List.of(), TrueCondition.INSTANCE, List.of(), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemPickupAbility(List.of(new RunCommandAction(new ParsedCommands("say You got speed? Pick me up, baby!"))), List.of(), new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("speed")))))), List.of(), 5, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}