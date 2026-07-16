package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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
import java.util.Optional;

public class ActionOnFarmlandTrampleAbility extends Ability {

    public static final MapCodec<ActionOnFarmlandTrampleAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnFarmlandTrampleAbility::new));

    public final List<Action> entityActions;
    public final List<BlockAction> blockActions;
    public final List<BlockCondition> blockConditions;

    public ActionOnFarmlandTrampleAbility(List<Action> entityActions, List<BlockAction> blockActions, List<BlockCondition> blockCondition, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.blockActions = blockActions;
        this.blockConditions = blockCondition;
    }

    public boolean doesApply(Level level, BlockPos pos) {
        if (!blockConditions.isEmpty() && !BlockCondition.checkConditions(blockConditions, level, pos)) {
            return false;
        }

        return true;
    }

    public void runActions(LivingEntity entity, Level level, BlockPos pos) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
        if (!blockActions.isEmpty()) BlockAction.runList(blockActions, level, pos, Optional.empty());
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_FARMLAND_TRAMPLE.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnFarmlandTrampleAbility> {

        @Override
        public MapCodec<ActionOnFarmlandTrampleAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnFarmlandTrampleAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Farmland Trample")
                    .setDescription("Runs actions when the entity tramples a farmland block.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity that trampled the farmland.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "The block actions to run on the trampled farmland block.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only runs the actions if the farmland block fulfills these conditions.")
                    .addExampleObject(new ActionOnFarmlandTrampleAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on farmland trample!"))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}