package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.AsSourceAction;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.*;

public class ActionOnEntityCollisionAbility extends Ability {

    public static final MapCodec<ActionOnEntityCollisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions_on_first_tick", List.of()).forGetter(a -> a.entityActionsFirstTick),
            Action.LIST_CODEC.optionalFieldOf("entity_actions_on_tick", List.of()).forGetter(a -> a.entityActionsTick),
            Action.LIST_CODEC.optionalFieldOf("entity_actions_on_last_tick", List.of()).forGetter(a -> a.entityActionsLastTick),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnEntityCollisionAbility::new));

    public final List<Action> entityActionsFirstTick, entityActionsTick, entityActionsLastTick;
    public final Condition entityConditions;

    private final Set<UUID> currentlyColliding = new HashSet<>();

    public ActionOnEntityCollisionAbility(List<Action> entityActionsFirstTick, List<Action> entityActionsTick, List<Action> entityActionsLastTick, Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActionsFirstTick = entityActionsFirstTick;
        this.entityActionsTick = entityActionsTick;
        this.entityActionsLastTick = entityActionsLastTick;
        this.entityConditions = entityConditions;
    }

    public boolean doesApply(LivingEntity holder, Entity target, AbilityInstance<?> abilityInstance) {
        return entityConditions.test(DataContext.forAffected(target, holder, abilityInstance));
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (enabled && !entity.level().isClientSide()) {
            Set<UUID> nowColliding = new HashSet<>();

            for (Entity target : entity.level().getEntities(entity, entity.getBoundingBox(), e -> e != entity)) {
                if (!doesApply(entity, target, abilityInstance)) continue;

                UUID targetUUID = target.getUUID();
                nowColliding.add(targetUUID);

                DataContext context = DataContext.forAffected(target, entity, abilityInstance);

                if (!currentlyColliding.contains(targetUUID) && !entityActionsFirstTick.isEmpty()) {
                    Action.runList(entityActionsFirstTick, context);
                }

                if (!entityActionsTick.isEmpty()) {
                    Action.runList(entityActionsTick, context);
                }
            }

            if (!entityActionsLastTick.isEmpty()) {
                for (UUID previousUUID : currentlyColliding) {
                    if (!nowColliding.contains(previousUUID)) {
                        Entity previous = entity.level().getEntity(previousUUID);
                        if (previous != null) {
                            Action.runList(entityActionsLastTick, DataContext.forAffected(previous, entity, abilityInstance));
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
        if (!entityActionsLastTick.isEmpty() && !entity.level().isClientSide()) {
            for (UUID uuid : currentlyColliding) {
                Entity previous = entity.level().getEntity(uuid);
                if (previous != null) {
                    Action.runList(entityActionsLastTick, DataContext.forAffected(previous, entity, abilityInstance));
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
                    .setDescription("Performs actions when the entity collides with another entity. For this ability, the \"source\" is the entity that has this ability & the \"target\" is the entity being collided with.")
                    .addOptional("entity_actions_on_first_tick", TYPE_ACTION_LIST, "Action(s) that are performed on the target on first tick of collision.")
                    .addOptional("entity_actions_on_tick", TYPE_ACTION_LIST, "Action(s) that are performed on the target every tick while colliding with an entity.")
                    .addOptional("entity_actions_on_last_tick", TYPE_ACTION_LIST, "Action(s) that are performed on the target the last tick of collision.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) the target must meet for the actions to perform.")
                    .addExampleObject(new ActionOnEntityCollisionAbility(List.of(new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say first tick action on entity collision (source)!"))))), List.of(new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say tick action on entity collision (source)!"))))), List.of(new AsSourceAction(List.of(new RunCommandAction(new ParsedCommands("say last tick action on entity collision (source)!"))))), TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnEntityCollisionAbility(List.of(new RunCommandAction(new ParsedCommands("say first tick action on entity collision (target)!"))), List.of(new RunCommandAction(new ParsedCommands("say tick action on entity collision (target)!"))), List.of(new RunCommandAction(new ParsedCommands("say last tick action on entity collision (target)!"))), TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}