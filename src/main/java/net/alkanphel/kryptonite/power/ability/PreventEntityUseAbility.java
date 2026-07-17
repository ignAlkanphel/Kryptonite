package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.alkanphel.kryptonite.util.apoli.ability.InteractionPrioritizedAbility;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class PreventEntityUseAbility extends InteractionPrioritizedAbility {

    public static final MapCodec<PreventEntityUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            InteractionPrioritizedFields.CODEC.forGetter(a -> new InteractionPrioritizedFields(new InteractionFields(a.heldItemActions, a.heldItemConditions, a.resultItemActions, a.resultStack, a.hands, a.actionResult), a.getPriority())),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, (biEntityActions, biEntityConditions, activeFields, properties, state, energyBarUsages) -> {
        InteractionFields interaction = activeFields.interaction();
        return new PreventEntityUseAbility(biEntityActions, biEntityConditions, interaction.heldItemActions(), interaction.heldItemConditions(), interaction.resultItemActions(), interaction.resultStack(), interaction.hands(), interaction.actionResult(), activeFields.priority(), properties, state, energyBarUsages);
    }));

    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;

    public PreventEntityUseAbility(List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, List<ItemAction> heldItemActions, List<ItemCondition> heldItemConditions, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemActions, heldItemConditions, resultItemActions, resultStack, hands, actionResult, priority, properties, conditions, energyBarUsages);
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesApply(Entity holder, Entity other, InteractionHand hand, ItemStack heldStack) {
        if (!shouldRun(hand, heldStack)) {
            return false;
        }

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, other)) {
            return false;
        }

        return true;
    }

    public InteractionResult runActions(Entity holder, Entity other, InteractionHand hand) {
        if (holder instanceof LivingEntity living && !biEntityActions.isEmpty()) {
            BiAction.runList(biEntityActions, living, other);
        }

        if (holder instanceof Player player) {
            this.performActorItemStuff(player, hand);
        }

        return this.getActionResult();
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
            builder.setName("Prevent Entity Use")
                    .setDescription("Prevents the player that has this ability from \"using\" (right-clicking) an entity and runs actions upon being prevented. In the context of this ability, the \"actor\" is the entity that has this ability & \"target\" the entity that was \"used\" (right-clicked).")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these actions will be run on either or both \"actor\" & \"target\" entities.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the actions will only be run if these conditions are fulfilled by either or both \"actor\" & \"target\" entities.")
                    .addOptional("held_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these actions will be run on the item used by the \"actor\" entity for right-clicking the \"target\" entity.")
                    .addOptional("held_item_conditions", KryptoniteDocumented.TYPE_ITEM_CONDITION_LIST, "If specified, the actions will only be run if these conditions are fulfilled by the item used by the \"actor\"' entity for right-clicking the \"target\" entity.")
                    .addOptional("result_item_actions", KryptoniteDocumented.TYPE_ITEM_ACTION_LIST, "If specified, these actions will be run on the item that is given to the \"actor\" entity.")
                    .addOptional("result_stack", TYPE_ITEM_STACK, "If specified, this item stack will be given to the \"actor\" entity.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "Determines if this ability should activate if the \"actor\" entity used the specified hands.")
                    .addOptional("action_result", KryptoniteDocumented.TYPE_INTERACTION_RESULT, "Used to indicate the result of a certain action.")
                    .addOptional("priority", TYPE_INT, "The run priority of this ability. Higher priorities of this ability run first.", 0)
                    .addExampleObject(new PreventEntityUseAbility(List.of(), List.of(), List.of(), List.of(), List.of(), Optional.empty(), EnumSet.allOf(InteractionHand.class), InteractionResult.SUCCESS, 0, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}