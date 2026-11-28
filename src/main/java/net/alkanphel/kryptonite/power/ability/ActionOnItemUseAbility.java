package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.ItemItemCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.ability.Prioritized;
import net.alkanphel.kryptonite.util.apoli.ability.PriorityPhase;
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
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

public class ActionOnItemUseAbility extends Ability implements Prioritized {

    public static final MapCodec<ActionOnItemUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemAction),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemCondition),
            TriggerType.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("trigger", EnumSet.allOf(TriggerType.class)).forGetter(a -> a.triggerType),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemUseAbility::new));

    private final List<Action> action;
    private final List<ItemAction> itemAction;
    private final List<ItemCondition> itemCondition;
    private final EnumSet<TriggerType> triggerType;

    private final int priority;

    public ActionOnItemUseAbility(List<Action> action, List<ItemAction> itemAction, List<ItemCondition> itemCondition, EnumSet<TriggerType> triggerType, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
        this.itemAction = itemAction;
        this.itemCondition = itemCondition;
        this.triggerType = triggerType;
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return this.priority;
    }

    public boolean doesApply(LivingEntity holder, ItemStack stack, EnumSet<TriggerType> triggerTypes, PriorityPhase priorityPhase) {
        if (Collections.disjoint(this.triggerType, triggerTypes) || !priorityPhase.test(this.getPriority())) {
            return false;
        }

        return itemCondition.isEmpty() || ItemCondition.checkConditions(itemCondition, holder.level(), stack);
    }

    public void runActions(DataContext context, SlotAccess stackReference) {
        var holder = context.getLivingEntity();
        if (holder == null) return;

        if (!itemAction.isEmpty()) ItemAction.runList(itemAction, holder.level(), stackReference);
        if (!action.isEmpty()) Action.runList(action, DataContext.forEntity(holder));
    }

    public static void apply(LivingEntity user, ItemStack stack, EnumSet<TriggerType> trigger, PriorityPhase phase) {
        if (user.level().isClientSide()) return;

        List<ActionOnItemUseAbility> abilities = Prioritized.collect(user, ActionOnItemUseAbility.class,
                ability -> ability.doesApply(user, stack, trigger, phase));

        DataContext context = DataContext.forEntity(user);
        SlotAccess stackReference = SlotAccess.of(() -> stack, _ -> {});
        abilities.forEach(ability -> ability.runActions(context, stackReference));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_ITEM_USE.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnItemUseAbility> {

        @Override
        public MapCodec<ActionOnItemUseAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnItemUseAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the player uses an item (e.g. drawing a bow or eating).")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player after using an item.")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the remaining item.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Item condition(s) the remaining item must meet for the actions to perform.")
                    .addOptional("trigger", SettingType.enumList(TriggerType.values()), "At which point the actions perform.", TriggerType.FINISH)
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Stopped drawing bow!")))), List.of(), List.of(new ItemItemCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("bow"))))))), EnumSet.of(TriggerType.FINISH), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Ender Pearl thrown!")))), List.of(), List.of(new ItemItemCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("ender_pearl"))))))), EnumSet.of(TriggerType.INSTANT), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Finished eating apple!")))), List.of(), List.of(new ItemItemCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple"))))))), EnumSet.of(TriggerType.FINISH), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum TriggerType implements StringRepresentable {
        START, TICK, STOP, FINISH, INSTANT;

        public static final Codec<TriggerType> CODEC = StringRepresentable.fromEnum(TriggerType::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}