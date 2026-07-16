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
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnItemDropAbility extends Ability {

    public static final MapCodec<ActionOnItemDropAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            ItemAction.LIST_CODEC.optionalFieldOf("item_actions", List.of()).forGetter(a -> a.itemActions),
            ItemCondition.LIST_CODEC.optionalFieldOf("item_conditions", List.of()).forGetter(a -> a.itemConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnItemDropAbility::new));

    private final List<Action> entityActions;
    private final List<ItemAction> itemActions;
    private final List<ItemCondition> itemConditions;

    public ActionOnItemDropAbility(List<Action> entityActions, List<ItemAction> itemActions, List<ItemCondition> itemConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.itemActions = itemActions;
        this.itemConditions = itemConditions;
    }

    public boolean doesApply(LivingEntity holder, ItemStack stack) {
        if (!itemConditions.isEmpty() && !ItemCondition.checkConditions(itemConditions, holder.level(), stack)) {
            return false;
        }

        return true;
    }

    public void runActions(LivingEntity holder, SlotAccess slotAccess) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(holder));
        if (!itemActions.isEmpty()) ItemAction.runList(itemActions, holder.level(), slotAccess);
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
            builder.setName("Action On Item Drop")
                    .setDescription("Runs actions when the player throws/drops an item.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the player when they drop an item.")
                    .addOptional("item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "The item actions to run on the dropped item.")
                    .addOptional("item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, only enables if the dropped item fulfills these item conditions.")
                    .addExampleObject(new ActionOnItemDropAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dropped an item!")))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnItemDropAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Dropped a diamond!")))), List.of(), List.of(new ItemItemCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ITEM, Identifier.withDefaultNamespace("diamond")))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}