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
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnItemFishedAbility extends Ability {

    public static final MapCodec<ActionOnItemFishedAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            ItemMutation.CODEC.optionalFieldOf("item_mutations", ItemMutation.EMPTY).forGetter(a -> a.itemMutation),
            ItemFilter.CODEC.optionalFieldOf("item_filters", ItemFilter.EMPTY).forGetter(a -> a.itemFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemFishedAbility::new));

    private final List<Action> entityActions;
    private final ItemMutation itemMutation;
    private final ItemFilter itemFilter;

    public ActionOnItemFishedAbility(List<Action> entityActions, ItemMutation itemMutation, ItemFilter itemFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
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
        return KryptoniteAbilitySerializers.ACTION_ON_ITEM_FISHED.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnItemFishedAbility> {

        @Override
        public MapCodec<ActionOnItemFishedAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnItemFishedAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the player reels in an item while fishing.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player after reeling in an item.")
                    .addOptional("item_mutations", ItemMutation.documentation(provider), "Item mutation(s) conducted on the reeled item.")
                    .addOptional("item_filters", ItemFilter.documentation(provider), "Item filter(s) the reeled item must pass for the actions to perform.")
                    .addExampleObject(new ActionOnItemFishedAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say You fished up an item!")))), ItemMutation.EMPTY, ItemFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemFishedAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say You fished up raw cod!")))), ItemMutation.EMPTY, ItemFilter.ofItem(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("cod")))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}