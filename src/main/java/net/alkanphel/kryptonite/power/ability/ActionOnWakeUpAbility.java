package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;
import java.util.Optional;

public class ActionOnWakeUpAbility extends Ability {

    public static final MapCodec<ActionOnWakeUpAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnWakeUpAbility::new));

    public final List<Action> entityActions;
    public final List<BlockAction> blockActions;
    public final List<BlockCondition> blockConditions;

    public ActionOnWakeUpAbility(List<Action> entityActions, List<BlockAction> blockActions, List<BlockCondition> blockConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.blockActions = blockActions;
        this.blockConditions = blockConditions;
    }

    public boolean doesApply(BlockPos pos, LivingEntity entity) {
        if (!blockConditions.isEmpty() && !BlockCondition.checkConditions(blockConditions, entity.level(), pos)) {
            return false;
        }

        return true;
    }

    public void runActions(BlockPos pos, Direction direction, LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
        if (!blockActions.isEmpty()) BlockAction.runList(blockActions, entity.level(), pos, Optional.ofNullable(direction));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_WAKE_UP.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnWakeUpAbility> {

        @Override
        public MapCodec<ActionOnWakeUpAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnWakeUpAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Wake Up")
                    .setDescription("Run actions when the entity wakes up from sleeping.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity when waking up.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "The block actions to run on the block the entity was sleeping on.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only runs actions if the sleeping block fulfills these conditions.")
                    .addExampleObject(new ActionOnWakeUpAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on wake up!"))), List.of(), List.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("red_bed")))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}