package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
import net.alkanphel.kryptonite.power.logic.mutation.ItemMutation;
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
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            ItemMutation.CODEC.optionalFieldOf("item_mutations", ItemMutation.EMPTY).forGetter(a -> a.itemMutation),
            ItemFilter.CODEC.optionalFieldOf("item_filters", ItemFilter.EMPTY).forGetter(a -> a.itemFilter),
            TriggerType.CODEC.listOf().xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("trigger", EnumSet.allOf(TriggerType.class)).forGetter(a -> a.triggerType),
            Codec.INT.optionalFieldOf("priority", 0).forGetter(a -> a.priority),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemUseAbility::new));

    private final List<Action> entityActions;
    private final ItemMutation itemMutation;
    private final ItemFilter itemFilter;
    private final EnumSet<TriggerType> triggerType;

    private final int priority;

    public ActionOnItemUseAbility(List<Action> entityActions, ItemMutation itemMutation, ItemFilter itemFilter, EnumSet<TriggerType> triggerType, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.itemMutation = itemMutation;
        this.itemFilter = itemFilter;
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

        return itemFilter.test(holder.level(), stack);
    }

    public void runActions(DataContext context, SlotAccess stackReference) {
        var holder = context.getLivingEntity();
        if (holder == null) return;

        if (!itemMutation.mutations().isEmpty()) itemMutation.run(holder.level(), stackReference);
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
    }

    public static void apply(LivingEntity user, ItemStack stack, EnumSet<TriggerType> trigger, PriorityPhase phase) {
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
            builder.setDescription("Performs actions when the player uses an item (e.g. drawing a bow or eating).")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player after using an item.")
                    .addOptional("item_mutations", ItemMutation.documentation(provider), "Item mutation(s) conducted on the remaining item.")
                    .addOptional("item_filters", ItemFilter.documentation(provider), "Item filters(s) the remaining item must pass for the actions to perform.")
                    .addOptional("trigger", SettingType.enumList(TriggerType.values()), "At which point the actions perform.", TriggerType.FINISH)
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Stopped drawing bow!")))), ItemMutation.EMPTY, ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("bow"))))))), EnumSet.of(TriggerType.FINISH), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Ender Pearl thrown!")))), ItemMutation.EMPTY, ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("ender_pearl"))))))), EnumSet.of(TriggerType.INSTANT), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemUseAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Finished eating apple!")))), ItemMutation.EMPTY, ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("apple"))))))), EnumSet.of(TriggerType.FINISH), 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
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