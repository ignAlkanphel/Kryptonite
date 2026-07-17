package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.ActorActionBiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.TargetActionBiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.*;

public class ActionOnEntityCollisionAbility extends Ability {

    public static final MapCodec<ActionOnEntityCollisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions_on_first_tick", List.of()).forGetter(a -> a.biEntityActionsFirstTick),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions_on_tick", List.of()).forGetter(a -> a.biEntityActionsTick),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions_on_last_tick", List.of()).forGetter(a -> a.biEntityActionsTick),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnEntityCollisionAbility::new));

    public final List<BiAction> biEntityActionsFirstTick, biEntityActionsTick, biEntityActionsLastTick;
    public final List<BiCondition> biEntityConditions;

    private final Set<UUID> currentlyColliding = new HashSet<>();

    public ActionOnEntityCollisionAbility(List<BiAction> biEntityActionsFirstTick, List<BiAction> biEntityActionsTick, List<BiAction> biEntityActionsLastTick, List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityActionsFirstTick = biEntityActionsFirstTick;
        this.biEntityActionsTick = biEntityActionsTick;
        this.biEntityActionsLastTick = biEntityActionsLastTick;
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesApply(LivingEntity holder, Entity target) {
        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, target)) {
            return false;
        }

        return true;
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled && !entity.level().isClientSide()) {
            Set<UUID> nowColliding = new HashSet<>();

            for (Entity target : entity.level().getEntities(entity, entity.getBoundingBox(), e -> e != entity)) {
                if (!doesApply(entity, target)) continue;

                UUID targetUUID = target.getUUID();
                nowColliding.add(targetUUID);

                if (!currentlyColliding.contains(targetUUID) && !biEntityActionsFirstTick.isEmpty()) {
                    BiAction.runList(biEntityActionsFirstTick, entity, target);
                }

                if (!biEntityActionsTick.isEmpty()) {
                    BiAction.runList(biEntityActionsTick, entity, target);
                }
            }

            if (!biEntityActionsLastTick.isEmpty()) {
                for (UUID previousUUID : currentlyColliding) {
                    if (!nowColliding.contains(previousUUID)) {
                        Entity previous = entity.level().getEntity(previousUUID);
                        if (previous != null) {
                            BiAction.runList(biEntityActionsLastTick, entity, previous);
                        }
                    }
                }
            }

            currentlyColliding.clear();
            currentlyColliding.addAll(nowColliding);
        }

        return super.tick(entity, abilityInstance, enabled);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        currentlyColliding.clear();
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (!biEntityActionsLastTick.isEmpty() && !entity.level().isClientSide()) {
            for (UUID uuid : currentlyColliding) {
                Entity previous = entity.level().getEntity(uuid);
                if (previous != null) {
                    BiAction.runList(biEntityActionsLastTick, entity, previous);
                }
            }
        }

        currentlyColliding.clear();
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_ENTITY_COLLISION.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnEntityCollisionAbility> {

        @Override
        public MapCodec<ActionOnEntityCollisionAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnEntityCollisionAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Entity Collision")
                    .setDescription("Runs actions when the entity collides with another entity. The 'actor' is the entity that has the ability whilst the 'target' is the entity collided with.")
                    .addOptional("bientity_actions_on_first_tick", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on first tick while collision with an entity.")
                    .addOptional("bientity_actions_on_tick", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run every tick while colliding with an entity.")
                    .addOptional("bientity_actions_on_last_tick", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on last tick while colliding with an entity.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, the actions will only run if these bi conditions are fulfilled by either or both 'actor' & 'target' entities.")
                    .addExampleObject(new ActionOnEntityCollisionAbility(List.of(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say first tick action on entity collision (actor_action)!"))))), List.of(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say tick action on entity collision (actor_action)!"))))), List.of(new ActorActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say last tick action on entity collision (actor_action)!"))))), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnEntityCollisionAbility(List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say first tick action on entity collision (target_action)!"))))), List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say tick action on entity collision (target_action)!"))))), List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands("say last tick action on entity collision (target_action)!"))))), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}