package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PreventEntityUseAbility extends InteractionPrioritizedAbility {

    public static final MapCodec<PreventEntityUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TargetUsed.CODEC.optionalFieldOf("target_used", TargetUsed.SOURCE).forGetter(a -> a.targetUsed),
            Action.LIST_CODEC.optionalFieldOf("target_actions", List.of()).forGetter(a -> a.action),
            Condition.CODEC.optionalFieldOf("target_conditions", TrueCondition.INSTANCE).forGetter(a -> a.condition),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemCondition, a.heldItemAction, a.resultItemAction, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (targetUsed, action, condition, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new PreventEntityUseAbility(targetUsed, action, condition, interaction.heldItemCondition(), interaction.heldItemAction(), interaction.resultItemAction(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final TargetUsed targetUsed;
    public final List<Action> action;
    public final Condition condition;

    public PreventEntityUseAbility(TargetUsed targetUsed, List<Action> action, Condition condition, List<ItemCondition> heldItemCondition, List<ItemAction> heldItemAction, List<ItemAction> resultItemAction, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemCondition, heldItemAction, resultItemAction, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.targetUsed = targetUsed;
        this.action = action;
        this.condition = condition;
    }

    public boolean doesApply(TargetUsed role, Entity source, Entity target, InteractionHand hand, ItemStack heldStack) {
        if (this.targetUsed != role || !shouldRun(hand, heldStack)) {
            return false;
        }

        return condition.test(DataContext.forAffected(target, source, null));
    }

    public void runActions(Entity source, Entity target, InteractionHand hand) {
        if (source instanceof LivingEntity && !action.isEmpty()) {
            Action.runList(action, DataContext.forAffected(source, target, null));
        }

        if (source instanceof Player player) {
            this.performSourceItemStuff(player, hand);
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_ENTITY_USE.get();
    }

    public static class Serializer extends AbilitySerializer<PreventEntityUseAbility> {

        @Override
        public MapCodec<PreventEntityUseAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventEntityUseAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents a player from \"using\" (right-clicking) an entity and performs actions upon being prevented. For this ability, the \"source\" is the player performing the interaction & the \"target\" is the entity being interacted with. Which one of them needs to have this ability is determined by \"target_used\".")
                    .addOptional("target_used", SettingType.enumList(TargetUsed.values()), "Determines which entity needs to have this ability. If \"source\", the player with this ability is prevented from using an entity. If \"target\", the entity with this ability is prevented from being used by a player.", TargetUsed.SOURCE)
                    .addOptional("target_actions", TYPE_ACTION_LIST, "Action(s) performed on the source when entity use is prevented.")
                    .addOptional("target_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet for entity use to be prevented.")
                    .addOptional("held_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item used by the source to interact with the target.")
                    .addOptional("held_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "Item condition(s) the item used by the source must meet for entity use to be prevented.")
                    .addOptional("result_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "Item action(s) performed on the item given to the source.")
                    .addOptional("result_stack", TYPE_ITEM_STACK, "The item stack given to the source.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "Determines if this ability should activate if the source used the specified hands.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities run first, and the first match decides the result.", 0)
                    .addExampleObject(new PreventEntityUseAbility(TargetUsed.SOURCE, List.of(), TrueCondition.INSTANCE, List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntityUseAbility(TargetUsed.TARGET, List.of(), TrueCondition.INSTANCE, List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventEntityUseAbility(TargetUsed.TARGET, List.of(new RunCommandAction(new ParsedCommands("title @s actionbar {\"text\": \"Entity cannot be interacted with!\", \"color\": \"red\"}"))), TrueCondition.INSTANCE, List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum TargetUsed implements StringRepresentable {
        SOURCE, // prevent entity use
        TARGET; // prevent being used

        public static final Codec<TargetUsed> CODEC = StringRepresentable.fromEnum(TargetUsed::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}