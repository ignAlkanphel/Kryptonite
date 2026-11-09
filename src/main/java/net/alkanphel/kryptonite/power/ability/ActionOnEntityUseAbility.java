package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.alkanphel.kryptonite.util.apoli.ability.PriorityPhase;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class ActionOnEntityUseAbility extends InteractionPrioritizedAbility {

    public static final MapCodec<ActionOnEntityUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetUsed.CODEC.optionalFieldOf("target_used", TargetUsed.SOURCE).forGetter(a -> a.targetUsed),
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemActions, a.heldItemConditions, a.resultItemActions, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (targetUsed, entityActions, entityConditions, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new ActionOnEntityUseAbility(targetUsed, entityActions, entityConditions, interaction.heldItemActions(), interaction.heldItemConditions(), interaction.resultItemActions(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final TargetUsed targetUsed;
    public final List<Action> entityActions;
    public final Condition entityConditions;

    public ActionOnEntityUseAbility(TargetUsed targetUsed, List<Action> entityActions, Condition entityConditions, List<ItemAction> heldItemActions, List<ItemCondition> heldItemConditions, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemActions, heldItemConditions, resultItemActions, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.targetUsed = targetUsed;
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
    }

    public boolean shouldRun(TargetUsed role, Player source, Entity target, InteractionHand hand, ItemStack heldStack, PriorityPhase priorityPhase) {
        if (this.targetUsed != role || !priorityPhase.test(this.getPriority()) || !super.shouldRun(hand, heldStack)) {
            return false;
        }

        return entityConditions.test(DataContext.forAffected(target, source, null));
    }

    public InteractionResult runActions(Player source, Entity target, InteractionHand hand) {
        if (!entityActions.isEmpty()) {
            Action.runList(entityActions, DataContext.forAffected(source, target, null));
        }

        performSourceItemStuff(source, hand);

        return this.getActionResult();
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_ENTITY_USE.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnEntityUseAbility> {

        @Override
        public MapCodec<ActionOnEntityUseAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnEntityUseAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Entity Use")
                    .setDescription("Performs actions when a player \"uses\" (right-clicks) an entity. For this ability, the \"source\" is the player performing the interaction & the \"target\" is the entity being interacted with. Which one of them needs to have this ability is determined by \"target_used\".")
                    .addOptional("target_used", SettingType.enumList(TargetUsed.values()), "Determines which entity needs to have this ability. If \"source\", the actions perform when the entity with this ability uses another entity. If \"target\", the actions perform when the entity with this ability is used by a player.", TargetUsed.SOURCE)
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the source when an entity is used.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet for the actions to performs.")
                    .addOptional("held_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Action(s) performed on the item used by the source to interact with the target.")
                    .addOptional("held_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Condition(s) that the item used by the source must meet for the actions to perform.")
                    .addOptional("result_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Action(s) performed on the item given to the source.")
                    .addOptional("result_stack", TYPE_ITEM_STACK, "If specified, this item stack will be given to the source.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "Determines if this ability should activate if the source used the specified hands.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new ActionOnEntityUseAbility(TargetUsed.SOURCE, List.of(), TrueCondition.INSTANCE, List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnEntityUseAbility(TargetUsed.TARGET, List.of(new RunCommandAction(new ParsedCommands("title @s actionbar {\"text\": \"Action on source when interacting with entity!\", \"color\": \"red\"}"))), TrueCondition.INSTANCE, List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum TargetUsed implements StringRepresentable {
        SOURCE, // action on entity use
        TARGET; // action on being used

        public static final Codec<TargetUsed> CODEC = StringRepresentable.fromEnum(TargetUsed::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}