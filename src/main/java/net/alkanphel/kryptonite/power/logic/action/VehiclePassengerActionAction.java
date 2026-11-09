package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;

public class VehiclePassengerActionAction extends Action {

    public static final MapCodec<VehiclePassengerActionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", net.threetag.palladium.logic.condition.TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            Value.CODEC.optionalFieldOf("recursive", new StaticValue(false)).forGetter(a -> a.recursive)
    ).apply(instance, VehiclePassengerActionAction::new));

    private final List<Action> entityActions;
    private final Condition entityConditions;
    private final Value recursive;

    public VehiclePassengerActionAction(List<Action> entityActions, Condition entityConditions, Value recursive) {
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
        this.recursive = recursive;
    }

    @Override
    protected boolean execute(DataContext context) {
        Entity entity = context.get(DataContextKeys.ENTITY);
        if (entity == null || !entity.isVehicle()) return false;

        Iterable<Entity> passengers = recursive.getAsBoolean(context)
                ? entity.getIndirectPassengers()
                : entity.getPassengers();

        for (Entity passenger : passengers) {
            DataContext passengerContext = DataContext.forAffected(passenger, entity, null);

            if (entityConditions.test(passengerContext)) {
                Action.runList(entityActions, passengerContext);
            }
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.VEHICLE_PASSENGER_ACTION.get();
    }

    public static class Serializer extends ActionSerializer<VehiclePassengerActionAction> {

        @Override
        public MapCodec<VehiclePassengerActionAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, VehiclePassengerActionAction> builder, HolderLookup.Provider provider) {
            builder.setName("Vehicle Passenger Action")
                    .setDescription("Performs actions on the passenger of the entity being ridden. For this action, the \"source\" is the vehicle and the \"target\" is one of its passengers.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on each passenger.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that each passenger must meet for the actions to perform.")
                    .addOptional("recursive", KryptoniteDocumented.TYPE_BOOLEAN_VALUE, "Whether to perform the actions on indirect passengers.", false)
                    .addExampleObject(new VehiclePassengerActionAction(List.of(), net.threetag.palladium.logic.condition.TrueCondition.INSTANCE, new StaticValue(false)));
        }
    }

}