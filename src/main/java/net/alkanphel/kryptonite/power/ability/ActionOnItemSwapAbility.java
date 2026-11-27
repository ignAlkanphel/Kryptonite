package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.ItemFilter;
import net.alkanphel.kryptonite.power.logic.mutation.ItemMutation;
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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class ActionOnItemSwapAbility extends Ability {

    public static final MapCodec<ActionOnItemSwapAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            ItemMutation.CODEC.optionalFieldOf("main_item_mutations", ItemMutation.EMPTY).forGetter(a -> a.mainItemMutation),
            ItemMutation.CODEC.optionalFieldOf("off_item_mutations", ItemMutation.EMPTY).forGetter(a -> a.offItemMutation),
            ItemFilter.CODEC.optionalFieldOf("main_item_filters", ItemFilter.EMPTY).forGetter(a -> a.mainItemFilter),
            ItemFilter.CODEC.optionalFieldOf("off_item_filters", ItemFilter.EMPTY).forGetter(a -> a.offItemFilter),
            TriggerType.CODEC.optionalFieldOf("trigger", TriggerType.BOTH).forGetter(a -> a.trigger),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemSwapAbility::new));

    private final List<Action> entityActions;
    private final ItemMutation mainItemMutation, offItemMutation;
    private final ItemFilter mainItemFilter, offItemFilter;
    private final TriggerType trigger;

    public ActionOnItemSwapAbility(List<Action> entityActions, ItemMutation mainItemMutation, ItemMutation offItemMutation, ItemFilter mainItemFilter, ItemFilter offItemFilter, TriggerType trigger, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.mainItemMutation = mainItemMutation;
        this.offItemMutation = offItemMutation;
        this.mainItemFilter = mainItemFilter;
        this.offItemFilter = offItemFilter;
        this.trigger = trigger;
    }

    public boolean doesApply(LivingEntity holder, ItemStack mainStack, ItemStack offStack) {
        boolean mainMatches = mainItemFilter.test(holder.level(), mainStack);
        boolean offMatches = offItemFilter.test(holder.level(), offStack);

        return switch (trigger) {
            case MAIN -> mainMatches;
            case OFF -> offMatches;
            case BOTH -> mainMatches && offMatches;
        };
    }

    public void runActions(LivingEntity holder, SlotAccess mainReference, SlotAccess offReference) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
        if (!mainItemMutation.mutations().isEmpty()) mainItemMutation.run(holder.level(), mainReference);
        if (!offItemMutation.mutations().isEmpty()) offItemMutation.run(holder.level(), offReference);
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
            builder.setDescription("Performs actions on the player upon swapping items between their main hand & off hand.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player when the swap filters pass.")
                    .addOptional("main_item_mutations", ItemMutation.documentation(provider), "Item mutation(s) conducted on the item swapped into the main hand.")
                    .addOptional("off_item_mutations", ItemMutation.documentation(provider), "Item mutation(s) conducted on the item swapped into the off hand.")
                    .addOptional("main_item_filters", ItemFilter.documentation(provider), "Item filters(s) the post-swap main hand item must pass.")
                    .addOptional("off_item_filters", ItemFilter.documentation(provider), "Item filters(s) the post-swap off hand item must pass.")
                    .addOptional("trigger", SettingType.enumList(TriggerType.values()), "Hand(s) whose item filters must pass for the actions to perform.", TriggerType.BOTH)
                    .addExampleObject(new ActionOnItemSwapAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dirt in main hand!")))), ItemMutation.EMPTY, ItemMutation.EMPTY, ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("dirt"))))))), ItemFilter.EMPTY, TriggerType.MAIN, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemSwapAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dirt in off hand!")))), ItemMutation.EMPTY, ItemMutation.EMPTY, ItemFilter.EMPTY, ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("dirt"))))))), TriggerType.OFF, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemSwapAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Diamond in main hand + Coal in off hand!")))), ItemMutation.EMPTY, ItemMutation.EMPTY, ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("diamond"))))))), ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("coal"))))))), TriggerType.BOTH, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum TriggerType implements StringRepresentable {
        MAIN, OFF, BOTH;

        public static final Codec<TriggerType> CODEC = StringRepresentable.fromEnum(TriggerType::values);

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}