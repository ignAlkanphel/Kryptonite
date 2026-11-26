package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.context.DimensionConditionContext;
import net.alkanphel.kryptonite.power.logic.filter.DimensionFilter;
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
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class PreventTeleportAbility extends Ability {

    public static final MapCodec<PreventTeleportAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.entityActions),
            DimensionFilter.CODEC.optionalFieldOf("from_dimension_filters", DimensionFilter.EMPTY).forGetter(a -> a.originDimensionFilters),
            DimensionFilter.CODEC.optionalFieldOf("to_dimension_filters", DimensionFilter.EMPTY).forGetter(a -> a.destinationDimensionFilters),
            Value.NUMBER_CODEC.optionalFieldOf("min_distance").forGetter(a -> a.minDistance),
            Value.NUMBER_CODEC.optionalFieldOf("max_distance").forGetter(a -> a.maxDistance),
            Source.CODEC.fieldOf("source").forGetter(a -> a.source),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventTeleportAbility::new));

    public final List<Action> entityActions;
    public final DimensionFilter originDimensionFilters, destinationDimensionFilters;
    public final Optional<Value> minDistance, maxDistance;
    public final Source source;

    public PreventTeleportAbility(List<Action> entityActions, DimensionFilter originDimensionFilters, DimensionFilter destinationDimensionFilters, Optional<Value> minDistance, Optional<Value> maxDistance, Source source, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.originDimensionFilters = originDimensionFilters;
        this.destinationDimensionFilters = destinationDimensionFilters;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.source = source;
    }

    public boolean doesApply(LivingEntity entity, AbilityInstance<?> instance, ServerLevel destinationLevel, Vec3 destination) {
        Vec3 origin = entity.position();
        Level originLevel = entity.level();

        if (!originDimensionFilters.test(new DimensionConditionContext(originLevel.dimensionType(), originLevel, entity))) {
            return false;
        }

        if (!destinationDimensionFilters.test(new DimensionConditionContext(destinationLevel.dimensionType(), destinationLevel, entity))) {
            return false;
        }

        double distance = origin.distanceTo(destination);

        if (minDistance.isPresent() && distance < minDistance.get().getAsDouble(DataContext.forAbility(entity, instance))) {
            return false;
        }

        if (maxDistance.isPresent() && distance > maxDistance.get().getAsDouble(DataContext.forAbility(entity, instance))) {
            return false;
        }

        return true;
    }

    public boolean doesApply(LivingEntity entity, ResourceKey<Level> destination) {
        Level originLevel = entity.level();

        if (!originDimensionFilters.test(new DimensionConditionContext(originLevel.dimensionType(), originLevel, entity))) {
            return false;
        }

        if (!destinationDimensionFilters.filters().isEmpty()) {
            if (originLevel instanceof ServerLevel serverLevel) {
                ServerLevel destinationLevel = serverLevel.getServer().getLevel(destination);
                if (destinationLevel == null) return false;
                return destinationDimensionFilters.test(new DimensionConditionContext(destinationLevel.dimensionType(), destinationLevel, null));
            }
        }

        return true;
    }

    public static boolean shouldPreventDimensionTravel(LivingEntity entity, ResourceKey<Level> destination) {
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

            builder.setDescription("Prevents the entity that has this ability from teleporting by the specified sources.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed upon teleportation being prevented.")
                    .addOptional("from_dimension_filters", DimensionFilter.documentation(provider), "Dimension filter(s) the dimension must pass for the dimension the entity is teleporting from.")
                    .addOptional("to_dimension_filters", DimensionFilter.documentation(provider), "Dimension filter(s) the dimension must pass for the dimension the entity is teleporting to.")
                    .addOptional("min_distance", TYPE_DOUBLE_VALUE, "Minimum distance required for prevention. Doesn't work for the 'dimension_travel' source.")
                    .addOptional("max_distance", TYPE_DOUBLE_VALUE, "Maximum distance required for prevention. Doesn't work for the 'dimension_travel' source.")
                    .add("source", SettingType.enumList(Source.values()), "The teleportation source to prevent.")
                    .addExampleObject(new PreventTeleportAbility(List.of(), DimensionFilter.EMPTY, DimensionFilter.EMPTY, Optional.empty(), Optional.empty(), Source.ENDER_PEARL, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventTeleportAbility(List.of(new RunCommandAction(new ParsedCommands("title @s actionbar {\"text\":\"You cannot enter The Nether from the Overworld!\"}"))), DimensionFilter.ofDimension(Level.OVERWORLD), DimensionFilter.ofDimension(Level.NETHER), Optional.empty(), Optional.empty(), Source.DIMENSION_TRAVEL, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
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
            return name().toLowerCase(Locale.ROOT);
        }
    }

}