package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

import java.util.Optional;

public record VehiclePassengerRecursiveCondition(Optional<BiCondition> biEntityConditions, NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<VehiclePassengerRecursiveCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.CODEC.optionalFieldOf("bientity_conditions").forGetter(c -> c.biEntityConditions),
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_THAN).forGetter(c -> c.comparator),
            Value.CODEC.optionalFieldOf("compare_to", new StaticValue(1)).forGetter(c -> c.compareTo)
    ).apply(instance, VehiclePassengerRecursiveCondition::new));

    @Override
    public boolean test(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        long matches = entity.getPassengers()
                .stream()
                .flatMap(Entity::getPassengersAndSelf)
                .filter(passenger -> biEntityConditions.map(condition -> condition.test(passenger, entity)).orElse(true))
                .count();

        return comparator.compare(matches, Math.max(1, compareTo.getAsInt(context)));
    }

    @Override
    public ConditionSerializer<?> getSerializer() {
        return KryptoniteConditionSerializers.VEHICLE_PASSENGER_RECURSIVE.get();
    }

    public static class Serializer extends ConditionSerializer<VehiclePassengerRecursiveCondition> {

        @Override
        public MapCodec<VehiclePassengerRecursiveCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, VehiclePassengerRecursiveCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Vehicle Passenger Recursive")
                    .setDescription("Checks how many passengers (including the passengers' passengers) are currently riding the entity. In the context of this condition, the \"actor\" entity/entities is/are the passenger(s) (and its passengers' passengers) and the \"target\" is the entity that fulfilled the condition.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only increase the amount of passengers if either or both the \"actor\" entity/entities and the \"target\" entity fulfills these conditions.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "How the amount of passengers (including the passengers' passengers) of the entity should be compared to the specified value.", NumberComparator.GREATER_THAN)
                    .addOptional("compare_to", KryptoniteSettingType.intValueRange(1, Integer.MAX_VALUE), "The value at which the amount of passengers (including the passengers' passengers) of the entity will be compared to.", 1)
                    .addExampleObject(new VehiclePassengerRecursiveCondition(Optional.empty(), NumberComparator.GREATER_THAN, new StaticValue(1)));
        }
    }

}