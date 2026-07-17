package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.SetBlockBlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.condition.block.BlockBlockCondition;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.alkanphel.kryptonite.util.apoli.SavedBlockPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
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

public class ActionOnBlockBreakAbility extends Ability {

    public static final MapCodec<ActionOnBlockBreakAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockConditions),
            Codec.BOOL.optionalFieldOf("only_when_harvested", false).forGetter(a -> a.onlyWhenHarvested),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnBlockBreakAbility::new));

    public final List<Action> entityActions;
    public final List<BlockAction> blockActions;
    public final List<BlockCondition> blockConditions;
    public final boolean onlyWhenHarvested;

    public ActionOnBlockBreakAbility(List<Action> entityActions, List<BlockAction> blockActions, List<BlockCondition> blockConditions, boolean onlyWhenHarvested, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.blockActions = blockActions;
        this.blockConditions = blockConditions;
        this.onlyWhenHarvested = onlyWhenHarvested;
    }

    public boolean doesApply(SavedBlockPosition savedBlock, boolean harvestedSuccessfully) {
        if (onlyWhenHarvested && !harvestedSuccessfully) {
            return false;
        }

        if (!blockConditions.isEmpty() && !BlockCondition.checkConditions(blockConditions, savedBlock)) {
            return false;
        }

        return true;
    }

    public void runActions(Player player, BlockPos pos, Direction direction) {
        if (!blockActions.isEmpty()) BlockAction.runList(blockActions, player.level(), pos, Optional.of(direction));

        if (!entityActions.isEmpty() && player.level() instanceof ServerLevel) {
            Action.runList(entityActions, DataContext.forEntity(player));
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_BLOCK_BREAK.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnBlockBreakAbility> {

        @Override
        public MapCodec<ActionOnBlockBreakAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnBlockBreakAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Block Break")
                    .setDescription("Runs actions when the player that has this ability breaks a block.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the player that broke the block.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "The block actions to run at the broken block position.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "If specified, only runs if the broken block fulfills these block conditions.")
                    .addOptional("only_when_harvested", TYPE_BOOLEAN, "If true, only runs when the block was successfully harvested (using correct tool for drops).", false)
                    .addExampleObject(new ActionOnBlockBreakAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Magma Block broken and converted to lava!")))), List.of(new SetBlockBlockAction(Blocks.LAVA.defaultBlockState())), List.of(new BlockBlockCondition(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("magma_block")))))), true, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}