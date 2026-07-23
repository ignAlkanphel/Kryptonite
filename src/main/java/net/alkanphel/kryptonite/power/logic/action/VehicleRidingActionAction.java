package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;

import java.util.List;
import java.util.Optional;

public class VehicleRidingActionAction extends Action {

    public static final MapCodec<VehicleRidingActionAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.actions),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.CODEC.optionalFieldOf("bientity_conditions").forGetter(a -> a.biEntityConditions),
            Value.CODEC.optionalFieldOf("recursive", new StaticValue(false)).forGetter(a -> a.recursive)
    ).apply(instance, VehicleRidingActionAction::new));

    private final List<Action> actions;
    private final List<BiAction> biEntityActions;
    private final Optional<BiCondition> biEntityConditions;
    private final Value recursive;

    public VehicleRidingActionAction(List<Action> actions, List<BiAction> biEntityActions, Optional<BiCondition> biEntityConditions, Value recursive) {
        this.actions = actions;
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
        this.recursive = recursive;
    }

    @Override
    public boolean run(DataContext context) {
        Entity entity = context.get(DataContextKeys.ENTITY);
        if (entity == null) return false;

        Entity vehicle = entity.getVehicle();

        while (vehicle != null) {
            Entity finalVehicle = vehicle;

            if (biEntityConditions.map(c -> c.test(entity, finalVehicle)).orElse(true)) {
                if (!actions.isEmpty()) Action.runList(actions, DataContext.forEntity(finalVehicle));
                if (!biEntityActions.isEmpty()) BiAction.runList(biEntityActions, entity, finalVehicle);
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
                    .setDescription("Runs actions on the entity/entities being ridden by said entity.")
                    .addOptional("actions", TYPE_ACTION_LIST, "If specified, runs the actions on the entity/entities being ridden.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, runs these actions on the entity/entities being ridden.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only runs the actions if these conditions are fulfilled.")
                    .addOptional("recursive", TYPE_VALUE, "If set to true, the specified actions will run on all entities that are being ridden.", false)
                    .addExampleObject(new VehicleRidingActionAction(List.of(), List.of(), Optional.empty(), new StaticValue(false)));
        }
    }

}