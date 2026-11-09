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
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;

public class VehicleRidingActionAction extends Action {

    public static final MapCodec<VehicleRidingActionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            Value.CODEC.optionalFieldOf("recursive", new StaticValue(false)).forGetter(a -> a.recursive)
    ).apply(instance, VehicleRidingActionAction::new));

    private final List<Action> entityActions;
    private final Condition entityConditions;
    private final Value recursive;

    public VehicleRidingActionAction(List<Action> entityActions, Condition entityConditions, Value recursive) {
        this.entityActions = entityActions;
        this.entityConditions = entityConditions;
        this.recursive = recursive;
    }

    @Override
    protected boolean execute(DataContext context) {
        Entity entity = context.get(DataContextKeys.ENTITY);
        if (entity == null) return false;

        Entity vehicle = entity.getVehicle();

        while (vehicle != null) {
            DataContext vehicleContext = DataContext.forAffected(vehicle, entity, null);

            if (entityConditions.test(vehicleContext)) {
                Action.runList(entityActions, vehicleContext);
            }

            if (!recursive.getAsBoolean(context)) break;
            vehicle = vehicle.getVehicle();
        }

        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.VEHICLE_RIDING_ACTION.get();
    }

    public static class Serializer extends ActionSerializer<VehicleRidingActionAction> {

        @Override
        public MapCodec<VehicleRidingActionAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, VehicleRidingActionAction> builder, HolderLookup.Provider provider) {
            builder.setName("Vehicle Riding Action")
                    .setDescription("Performs actions on the vehicles being ridden by the entity. For this action, the \"source\" is the rider and the \"target\" is each vehicle.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "Action(s) performed on each vehicle.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that each vehicle must meet for the actions to perform.")
                    .addOptional("recursive", KryptoniteDocumented.TYPE_BOOLEAN_VALUE, "Whether to perform the actions on vehicles further up the riding chain.", false)
                    .addExampleObject(new VehicleRidingActionAction(List.of(), TrueCondition.INSTANCE, new StaticValue(false)));
        }
    }

}