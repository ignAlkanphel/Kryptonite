package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.BlockFilter;
import net.alkanphel.kryptonite.power.logic.mutation.BlockMutation;
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

public class ActionOnBoneMealAbility extends Ability {

    public static final MapCodec<ActionOnBoneMealAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            BlockMutation.CODEC.optionalFieldOf("block_mutations", BlockMutation.EMPTY).forGetter(a -> a.blockMutation),
            BlockFilter.CODEC.optionalFieldOf("block_filters", BlockFilter.EMPTY).forGetter(a -> a.blockFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnBoneMealAbility::new));

    public final List<Action> entityActions;
    public final BlockMutation blockMutation;
    public final BlockFilter blockFilter;

    public ActionOnBoneMealAbility(List<Action> entityActions, BlockMutation blockMutation, BlockFilter blockFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.blockMutation = blockMutation;
        this.blockFilter = blockFilter;
    }

    public boolean doesApply(Level level, BlockPos pos) {
        return blockFilter.test(level, pos);
    }

    public void runActions(LivingEntity entity, Level level, BlockPos pos) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
        if (!blockMutation.mutations().isEmpty()) blockMutation.run(level, pos, Optional.empty());
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_BONE_MEAL.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnBoneMealAbility> {

        @Override
        public MapCodec<ActionOnBoneMealAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnBoneMealAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the player bone meals a block.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed upon bone mealing a block.")
                    .addOptional("block_mutations", BlockMutation.documentation(provider), "Block mutation(s) conducted on the bone mealed block.")
                    .addOptional("block_filters", BlockFilter.documentation(provider), "Block filter(s) that the bone mealed block must pass for the actions to perform.")
                    .addExampleObject(new ActionOnBoneMealAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on bone meal!"))), BlockMutation.EMPTY, BlockFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnBoneMealAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on bone meal! Aw, man!"))), BlockMutation.ofSpawnEntity(new BlockMutation.SummonMutation(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("creeper")))), List.of(new RunCommandAction(new ParsedCommands("effect give @s glowing 4 0 true"))), BlockMutation.SummonMutation.addExampleNbt())), BlockFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}