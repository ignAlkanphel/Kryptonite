package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.dimension.DimensionDimensionCondition;
import net.alkanphel.kryptonite.power.logic.condition.dimension.internal.DimensionCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class PreventTeleportAbility extends Ability {

    public static final MapCodec<PreventTeleportAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            DimensionCondition.LIST_CODEC.optionalFieldOf("from_dimension_conditions", List.of()).forGetter(a -> a.originDimensionConditions),
            DimensionCondition.LIST_CODEC.optionalFieldOf("to_dimension_conditions", List.of()).forGetter(a -> a.destinationDimensionConditions),
            Codec.DOUBLE.optionalFieldOf("min_distance").forGetter(a -> a.minDistance),
            Codec.DOUBLE.optionalFieldOf("max_distance").forGetter(a -> a.maxDistance),
            Source.CODEC.fieldOf("source").forGetter(a -> a.source),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventTeleportAbility::new));

    public final List<Action> entityActions;
    public final List<DimensionCondition> originDimensionConditions, destinationDimensionConditions;
    public final Optional<Double> minDistance, maxDistance;
    public final Source source;

    public PreventTeleportAbility(List<Action> entityActions, List<DimensionCondition> originDimensionConditions, List<DimensionCondition> destinationDimensionConditions, Optional<Double> minDistance, Optional<Double> maxDistance, Source source, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.originDimensionConditions = originDimensionConditions;
        this.destinationDimensionConditions = destinationDimensionConditions;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.source = source;
    }

    public boolean doesApply(LivingEntity entity, ServerLevel destinationLevel, Vec3 destination) {
        Vec3 origin = entity.position();

        if (!originDimensionConditions.isEmpty()) {
            if (!DimensionCondition.checkConditions(originDimensionConditions, entity.level().dimensionType(), entity.level(), entity)) {
                return false;
            }
        }

        if (!destinationDimensionConditions.isEmpty()) {
            if (!DimensionCondition.checkConditions(destinationDimensionConditions, destinationLevel.dimensionType(), destinationLevel, entity)) {
                return false;
            }
        }

        double distance = origin.distanceTo(destination);

        if (minDistance.isPresent() && distance < minDistance.get()) {
            return false;
        }

        if (maxDistance.isPresent() && distance > maxDistance.get()) {
            return false;
        }

        return true;
    }

    public boolean doesApply(LivingEntity entity, ResourceKey<Level> destination) {
        Level originLevel = entity.level();

        if (!originDimensionConditions.isEmpty()) {
            if (!DimensionCondition.checkConditions(originDimensionConditions, originLevel.dimensionType(), originLevel, entity)) {
                return false;
            }
        }

        if (!destinationDimensionConditions.isEmpty()) {
            if (originLevel instanceof ServerLevel serverLevel) {
                ServerLevel destinationLevel = serverLevel.getServer().getLevel(destination);
                if (destinationLevel == null) return false;
                if (!DimensionCondition.checkConditions(destinationDimensionConditions, destinationLevel.dimensionType(), destinationLevel, null)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static boolean doesPreventDimensionTravel(LivingEntity entity, ResourceKey<Level> destination) {
        boolean prevented = false;

        for (AbilityInstance<PreventTeleportAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_TELEPORT.get())) {
            var ability = instance.getAbility();
            if (ability.source != Source.DIMENSION_TRAVEL) continue;
            if (!ability.doesApply(entity, destination)) continue;

            ability.runActions(entity);
            prevented = true;
        }

        return prevented;
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_TELEPORT.get();
    }

    public static class Serializer extends AbilitySerializer<PreventTeleportAbility> {

        @Override
        public MapCodec<PreventTeleportAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventTeleportAbility> builder, HolderLookup.Provider provider) {

            builder.setDescription("Prevents teleporting by the specified sources for the entity that has this ability.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity upon teleportation being prevented.")
                    .addOptional("from_dimension_conditions", KryptoniteDocumented.TYPE_DIMENSION_CONDITION_LIST, "If specified, these conditions must be fulfilled for when the entity is teleporting from the dimension.")
                    .addOptional("to_dimension_conditions", KryptoniteDocumented.TYPE_DIMENSION_CONDITION_LIST, "If specified, these conditions must be fulfilled for when the entity is teleporting to the dimension.")
                    .addOptional("min_distance", TYPE_DOUBLE, "The minimum teleport distance required for prevention. Doesn't work for the 'dimension_travel' source.")
                    .addOptional("max_distance", TYPE_DOUBLE, "The maximum teleport distance required for prevention. Doesn't work for the 'dimension_travel' source.")
                    .add("source", SettingType.enumList(Source.values()), "The teleportation source to prevent.")
                    .addExampleObject(new PreventTeleportAbility(List.of(), List.of(), List.of(), Optional.empty(), Optional.empty(), Source.ENDER_PEARL, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventTeleportAbility(List.of(new RunCommandAction(new ParsedCommands("title @s actionbar {\"text\":\"You cannot enter The Nether from the Overworld!\"}"))), List.of(new DimensionDimensionCondition(Level.OVERWORLD)), List.of(new DimensionDimensionCondition(Level.NETHER)), Optional.empty(), Optional.empty(), Source.DIMENSION_TRAVEL, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum Source implements StringRepresentable {
        COMMAND_TELEPORT,
        COMMAND_SPREAD_PLAYERS,
        DIMENSION_TRAVEL,
        ENDER_ENTITY,
        ENDER_PEARL,
        ITEM_CONSUMPTION;

        public static final Codec<Source> CODEC = StringRepresentable.fromEnum(Source::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}