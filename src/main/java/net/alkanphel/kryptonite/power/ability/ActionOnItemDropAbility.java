package net.alkanphel.kryptonite.power.ability;

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
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnItemDropAbility extends Ability {

    public static final MapCodec<ActionOnItemDropAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            ItemMutation.CODEC.optionalFieldOf("item_mutations", ItemMutation.EMPTY).forGetter(a -> a.itemMutation),
            ItemFilter.CODEC.optionalFieldOf("item_filters", ItemFilter.EMPTY).forGetter(a -> a.itemFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemDropAbility::new));

    private final List<Action> entityActions;
    private final ItemMutation itemMutation;
    private final ItemFilter itemFilter;

    public ActionOnItemDropAbility(List<Action> entityActions, ItemMutation itemMutation, ItemFilter itemFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.itemMutation = itemMutation;
        this.itemFilter = itemFilter;
    }

    public boolean doesApply(LivingEntity holder, ItemStack stack) {
        return itemFilter.test(holder.level(), stack);
    }

    public void runActions(LivingEntity holder, SlotAccess slotAccess) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
        if (!itemMutation.mutations().isEmpty()) itemMutation.run(holder.level(), slotAccess);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_ITEM_DROP.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnItemDropAbility> {

        @Override
        public MapCodec<ActionOnItemDropAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnItemDropAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the player drops an item.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player when they drop an item.")
                    .addOptional("item_mutations", ItemMutation.documentation(provider), "Item mutation(s) conducted on the dropped item.")
                    .addOptional("item_filters", ItemFilter.documentation(provider), "Item filters(s) the dropped item must pass for the actions to perform.")
                    .addExampleObject(new ActionOnItemDropAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dropped an item!")))), ItemMutation.EMPTY, ItemFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemDropAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dropped a diamond!")))), ItemMutation.EMPTY, ItemFilter.ofItem((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("diamond"))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}