package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.block.internal.BlockAction;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
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
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;
import java.util.Optional;

public class ActionOnWakeUpAbility extends Ability {

    public static final MapCodec<ActionOnWakeUpAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            BlockAction.LIST_CODEC.optionalFieldOf("block_actions", List.of()).forGetter(a -> a.blockActions),
            BlockFilter.CODEC.optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(a -> a.blockFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnWakeUpAbility::new));

    public final List<Action> entityActions;
    public final List<BlockAction> blockActions;
    public final BlockFilter blockFilter;

    public ActionOnWakeUpAbility(List<Action> entityActions, List<BlockAction> blockActions, BlockFilter blockFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.blockActions = blockActions;
        this.blockFilter = blockFilter;
    }

    public boolean doesApply(BlockPos pos, LivingEntity entity) {
        return blockFilter.test(entity.level(), pos);
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
            builder.setDescription("Performs actions upon waking up after sleeping.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed on the player upon waking up.")
                    .addOptional("block_actions", KryptoniteDocumented.TYPE_BLOCK_ACTION_LIST, "Block action(s) performed on the block the player was sleeping on.")
                    .addOptional("block_filters", BlockFilter.documentation(provider), "Block filters(s) the sleeping block must pass for the actions to perform.")
                    .addExampleObject(new ActionOnWakeUpAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on wake up!"))), List.of(), BlockFilter.ofBlock((PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.BLOCK, Identifier.withDefaultNamespace("red_bed"))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}