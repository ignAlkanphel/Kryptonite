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
import net.threetag.palladium.util.ParsedCommands;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public class ActionOnItemUseAbility extends Ability implements Prioritized {

    public static final MapCodec<ActionOnItemUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemActions),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            TriggerType.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("trigger", EnumSet.allOf(TriggerType.class)).forGetter(a -> a.triggerType),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemUseAbility::new));

    private final List<Action> entityActions;
    private final List<ItemAction> itemActions;
    private final List<ItemCondition> itemConditions;
    private final EnumSet<TriggerType> triggerType;

    private final int priority;

    public ActionOnItemUseAbility(List<Action> entityActions, List<ItemAction> itemActions, List<ItemCondition> itemConditions, EnumSet<TriggerType> triggerType, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.itemActions = itemActions;
        this.itemConditions = itemConditions;
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

        if (!itemConditions.isEmpty() && !ItemCondition.checkConditions(itemConditions, holder.level(), stack)) {
            return false;
        }

        return true;
    }

    public void runActions(DataContext context, SlotAccess stackReference) {
        var holder = context.getLivingEntity();
        if (holder == null) return;

        if (!itemActions.isEmpty()) ItemAction.runList(itemActions, holder.level(), stackReference);
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
    }

    public static void run(LivingEntity user, ItemStack stack, EnumSet<TriggerType> trigger, PriorityPhase phase) {
        if (user.level().isClientSide()) return;

        Prioritized.CallInstance<ActionOnItemUseAbility> call = new Prioritized.CallInstance<>();
        call.add(user, ActionOnItemUseAbility.class, a -> a.doesApply(user, stack, trigger, phase));

        DataContext context = DataContext.forEntity(user);
        call.forEachByPriority(ability -> ability.runActions(context, SlotAccess.of(() -> stack, s -> {})));
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
            builder.setName("Action On Item Use")
                    .setDescription("Runs actions when the player uses an item (e.g. drawing a bow or eating food).")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "If specified, these actions will be run on the player after they use an item.")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these actions will be run on the remaining item.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, the actions will only run if these conditions are fulfilled by the item before use.")
                    .addOptional("trigger", SettingType.enumList(TriggerType.values()), "At which point the actions run.", TriggerType.FINISH)
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Stopped drawing bow!")))), List.of(), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("bow")))))), EnumSet.of(TriggerType.FINISH), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Ender Pearl thrown!")))), List.of(), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("ender_pearl")))))), EnumSet.of(TriggerType.INSTANT), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Finished eating apple!")))), List.of(), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple")))))), EnumSet.of(TriggerType.FINISH), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum TriggerType implements StringRepresentable {
        START, TICK, STOP, FINISH, INSTANT;

        public static final Codec<TriggerType> CODEC = StringRepresentable.fromEnum(TriggerType::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase();
        }
    }

}