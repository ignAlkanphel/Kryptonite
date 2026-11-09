package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnMountAbility extends Ability {

    public static final MapCodec<ActionOnMountAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Action.LIST_CODEC.optionalFieldOf("vehicle_actions", List.of()).forGetter(a -> a.vehicleActions),
            Condition.CODEC.optionalFieldOf("vehicle_conditions", TrueCondition.INSTANCE).forGetter(a -> a.vehicleConditions),
            Codec.BOOL.optionalFieldOf("switch_to_dismount", false).forGetter(a -> a.switchToDismount),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnMountAbility::new));

    public final List<Action> entityActions;
    public final List<Action> vehicleActions;
    public final Condition vehicleConditions;
    public final boolean switchToDismount;

    public ActionOnMountAbility(List<Action> entityActions, List<Action> vehicleActions, Condition vehicleConditions, boolean switchToDismount, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.vehicleActions = vehicleActions;
        this.vehicleConditions = vehicleConditions;
        this.switchToDismount = switchToDismount;
    }

    public boolean doesApply(LivingEntity source, Entity target, AbilityInstance<?> instance) {
        return vehicleConditions.test(DataContext.forAffected(target, source, instance));
    }

    public void runActions(LivingEntity source, Entity target, AbilityInstance<?> instance) {
        if (!entityActions.isEmpty()) {
            Action.runList(entityActions, DataContext.forEntity(source));
        }

        if (!vehicleActions.isEmpty()) {
            Action.runList(vehicleActions, DataContext.forAffected(target, source, instance));
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_MOUNT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnMountAbility> {

        @Override
        public MapCodec<ActionOnMountAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnMountAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Mount")
                    .setDescription("Performs actions when the entity that has this ability mounts or dismounts a vehicle. For this ability, the \"source\" is the entity mounting or dismounting & the \"target\" is the vehicle.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on the entity mounting or dismounting.")
                    .addOptional("vehicle_actions", TYPE_ACTION_LIST, "Action(s) performed on the vehicle.")
                    .addOptional("vehicle_conditions", TYPE_CONDITION_LIST, "Condition(s) that the vehicle must meet for the actions to perform.")
                    .addOptional("switch_to_dismount", TYPE_BOOLEAN, "If true, this ability will switch to dismount instead.", false)
                    .addExampleObject(new ActionOnMountAbility(List.of(new RunCommandAction(new ParsedCommands("say I mounted a vehicle!"))), List.of(), new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("oak_boat")))))), false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnMountAbility(List.of(new RunCommandAction(new ParsedCommands("say I dismounted a vehicle!"))), List.of(), new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("oak_boat")))))), true, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}