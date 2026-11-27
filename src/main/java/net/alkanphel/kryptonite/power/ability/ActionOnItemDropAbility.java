package net.alkanphel.kryptonite.power.ability;

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
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemAction),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemCondition),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemDropAbility::new));

    private final List<Action> action;
    private final List<ItemAction> itemAction;
    private final List<ItemCondition> itemCondition;

    public ActionOnItemDropAbility(List<Action> action, List<ItemAction> itemAction, List<ItemCondition> itemCondition, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
        this.itemAction = itemAction;
        this.itemCondition = itemCondition;
    }

    public boolean doesApply(LivingEntity holder, ItemStack stack) {
        return itemCondition.isEmpty() || ItemCondition.checkConditions(itemCondition, holder.level(), stack);
    }

    public void runActions(LivingEntity holder, SlotAccess slotAccess) {
        if (!action.isEmpty()) Action.runList(action, DataContext.forEntity(holder));
        if (!itemAction.isEmpty()) ItemAction.runList(itemAction, holder.level(), slotAccess);
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
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the dropped item.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Item condition(s) the dropped item must meet for the actions to perform.")
                    .addExampleObject(new ActionOnItemDropAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dropped an item!")))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemDropAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dropped a diamond!")))), List.of(), List.of(new ItemItemCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("diamond"))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}