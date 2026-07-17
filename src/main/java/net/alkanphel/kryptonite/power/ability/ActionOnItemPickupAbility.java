package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.TargetActionBiAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.Prioritized;
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
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.HasEffectCondition;
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
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemPickupAbility::new));

    private final List<BiAction> biEntityActions;
    private final List<ItemAction> itemActions;
    private final List<BiCondition> biEntityConditions;
    private final List<ItemCondition> itemConditions;
    private final int priority;

    public ActionOnItemPickupAbility(List<BiAction> biEntityActions, List<ItemAction> itemActions, List<BiCondition> biEntityConditions, List<ItemCondition> itemConditions, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityActions = biEntityActions;
        this.itemActions = itemActions;
        this.biEntityConditions = biEntityConditions;
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

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, thrower, holder)) {
            return false;
        }

        return true;
    }

    public void runBiActions(Entity thrower, Entity holder) {
        if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, thrower, holder);
    }

    public void runItemActions(Entity holder, SlotAccess stackReference) {
        if (!itemActions.isEmpty()) ItemAction.runList(itemActions, holder.level(), stackReference);
    }

    public static void runBiActions(CallInstance<ActionOnItemPickupAbility> callInstance, Entity throwerEntity, Entity holderEntity) {
        callInstance.forEachByPriority(ability -> ability.runBiActions(throwerEntity, holderEntity));
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
                    .setDescription("Runs actions upon the entity picking up an item. In the context of this ability, the \"actor\" is the entity that may have thrown the item & the \"target\" is the entity that picked up the item.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these bi actions will be run on either or both the \"actor\" & \"target\" entities.")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these item actions will be run on the item that was picked up.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, these actions will only be run if these bi conditions are fulfilled by either or both the \"actor & \"target\" entities.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, these actions will only be run if these item conditions are fulfilled by the item about to be picked up.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnItemPickupAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say AHHH!!! PICKED UP PICKED UP!!!"))))), List.of(), List.of(), List.of(), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemPickupAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say You got speed? Pick me up, baby!"))))), List.of(), List.of(new TargetConditionBiCondition(new HasEffectCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.MOB_EFFECT, Identifier.withDefaultNamespace("speed")))))))), List.of(), 5, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}