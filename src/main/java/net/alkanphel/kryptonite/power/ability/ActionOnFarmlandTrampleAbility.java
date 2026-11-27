package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.SummonBlockAction;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.condition.block.internal.BlockCondition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
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
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockAction),
            BlockCondition.LIST_CODEC.optionalFieldOf("block_conditions", List.of()).forGetter(a -> a.blockCondition),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnFarmlandTrampleAbility::new));

    public final List<Action> action;
    public final List<BlockAction> blockAction;
    public final List<BlockCondition> blockCondition;

    public ActionOnFarmlandTrampleAbility(List<Action> action, List<BlockAction> blockAction, List<BlockCondition> blockCondition, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
        this.blockAction = blockAction;
        this.blockCondition = blockCondition;
    }

    public boolean doesApply(Level level, BlockPos pos) {
        return blockCondition.isEmpty() || BlockCondition.checkConditions(blockCondition, level, pos);
    }

    public void runActions(LivingEntity entity, Level level, BlockPos pos) {
        if (!action.isEmpty()) Action.runList(action, DataContext.forEntity(entity));
        if (!blockAction.isEmpty()) BlockAction.runList(blockAction, level, pos, Optional.empty());
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
            builder.setDescription("Performs actions when trampling a farmland block.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed when trampling the farmland block.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "Block action(s) performed on the trampled farmland block.")
                    .addOptional("block_conditions", KryptoniteDocumented.TYPE_BLOCK_CONDITION_LIST, "Block condition(s) the farmland block must meet for the actions to perform.")
                    .addExampleObject(new ActionOnFarmlandTrampleAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on farmland trample!"))), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnFarmlandTrampleAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on farmland trample! Aw, man!"))), List.of(new SummonBlockAction(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("creeper")))), List.of(new RunCommandAction(new ParsedCommands("effect give @s glowing 4 0 true"))), SummonBlockAction.Serializer.addExampleNbt())), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}