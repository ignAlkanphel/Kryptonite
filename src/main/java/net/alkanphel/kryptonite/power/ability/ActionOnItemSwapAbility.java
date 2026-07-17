package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.ItemItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActionOnItemSwapAbility extends Ability {

    public static final MapCodec<ActionOnItemSwapAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            ItemAction.LIST_CODEC.optionalFieldOf("main_item_actions", List.of()).forGetter(a -> a.mainItemActions),
            ItemAction.LIST_CODEC.optionalFieldOf("off_item_actions", List.of()).forGetter(a -> a.offItemActions),
            ItemCondition.LIST_CODEC.optionalFieldOf("main_item_conditions", List.of()).forGetter(a -> a.mainItemConditions),
            ItemCondition.LIST_CODEC.optionalFieldOf("off_item_conditions", List.of()).forGetter(a -> a.offItemConditions),
            TriggerType.CODEC.optionalFieldOf("trigger", TriggerType.BOTH).forGetter(a -> a.trigger),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemSwapAbility::new));

    private final List<Action> entityActions;
    private final List<ItemAction> mainItemActions, offItemActions;
    private final List<ItemCondition> mainItemConditions, offItemConditions;
    private final TriggerType trigger;

    public ActionOnItemSwapAbility(List<Action> entityActions, List<ItemAction> mainItemActions, List<ItemAction> offItemActions, List<ItemCondition> mainItemConditions, List<ItemCondition> offItemConditions, TriggerType trigger, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.mainItemActions = mainItemActions;
        this.offItemActions = offItemActions;
        this.mainItemConditions = mainItemConditions;
        this.offItemConditions = offItemConditions;
        this.trigger = trigger;
    }

    public boolean doesApply(LivingEntity holder, ItemStack mainStack, ItemStack offStack) {
        boolean mainMatches = mainItemConditions.isEmpty() || ItemCondition.checkConditions(mainItemConditions, holder.level(), mainStack);
        boolean offMatches = offItemConditions.isEmpty() || ItemCondition.checkConditions(offItemConditions, holder.level(), offStack);

        return switch (trigger) {
            case MAIN -> mainMatches;
            case OFF -> offMatches;
            case BOTH -> mainMatches && offMatches;
        };
    }

    public void runActions(LivingEntity holder, SlotAccess mainReference, SlotAccess offReference) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
        if (!mainItemActions.isEmpty()) ItemAction.runList(mainItemActions, holder.level(), mainReference);
        if (!offItemActions.isEmpty()) ItemAction.runList(offItemActions, holder.level(), offReference);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_ITEM_SWAP.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnItemSwapAbility> {

        @Override
        public MapCodec<ActionOnItemSwapAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnItemSwapAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Item Swap")
                    .setDescription("Runs actions when a player swaps items between their main hand & off hand.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "If specified, these actions will be run on the player when the swap conditions are fulfilled.")
                    .addOptional("main_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these item actions will be run on the item swapped into the main hand.")
                    .addOptional("off_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these item actions will be run on the item swapped into the off hand.")
                    .addOptional("main_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, the main hand item after the swap must fulfill these conditions.")
                    .addOptional("off_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, the off hand item after the swap must fulfill these conditions.")
                    .addOptional("trigger", TYPE_STRING, "Which hands must fulfill their item conditions for the actions to run.", "both")
                    .addExampleObject(new ActionOnItemSwapAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dirt in main hand!")))), List.of(), List.of(), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("dirt")))))), List.of(), TriggerType.MAIN, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemSwapAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dirt in off hand!")))), List.of(), List.of(), List.of(), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("dirt")))))), TriggerType.OFF, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemSwapAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Diamond in main hand + Coal in off hand!")))), List.of(), List.of(), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("diamond")))))), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("coal")))))), TriggerType.BOTH, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum TriggerType implements StringRepresentable {
        MAIN, OFF, BOTH;

        public static final Codec<TriggerType> CODEC = StringRepresentable.fromEnum(TriggerType::values);

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

}