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

public record VehicleRidingRecursiveCondition(Optional<BiCondition> biEntityConditions, NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<VehicleRidingRecursiveCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.CODEC.optionalFieldOf("bientity_conditions").forGetter(c -> c.biEntityConditions),
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_THAN).forGetter(c -> c.comparator),
            Value.CODEC.optionalFieldOf("compare_to", new StaticValue(1)).forGetter(c -> c.compareTo)
    ).apply(instance, VehicleRidingRecursiveCondition::new));

    @Override
    public boolean test(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        Entity vehicle = entity.getVehicle();

        int matches = 0;

        while (vehicle != null) {
            final Entity finalVehicle = vehicle;
            if (biEntityConditions.map(condition -> condition.test(entity, finalVehicle)).orElse(true)) {
                ++matches;
            }

            vehicle = vehicle.getVehicle();
        }

        return comparator.compare(matches, Math.max(1, compareTo.getAsInt(context)));
    }

    @Override
    public ConditionSerializer<?> getSerializer() {
        return KryptoniteConditionSerializers.VEHICLE_RIDING_RECURSIVE.get();
    }

    public static class Serializer extends ConditionSerializer<VehicleRidingRecursiveCondition> {

        @Override
        public MapCodec<VehicleRidingRecursiveCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, VehicleRidingRecursiveCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Vehicle Riding Recursive")
                    .setDescription("Checks whether the \"actor\" entity is directly riding the \"target\" entity or the passenger(s) of the \"target\" entity. In the context of this condition, the \"actor\" is the passenger & the entity that fulfilled the condition, while the \"target\" entities are the entity that is being directly ridden and the passenger(s) of the said entity.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, these conditions must be fulfilled by either or both the \"actor\" & \"target\" entities.")
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "How the amount of entities currently being ridden should be compared to the specified value.", NumberComparator.GREATER_THAN)
                    .addOptional("compare_to", KryptoniteSettingType.intValueRange(1, Integer.MAX_VALUE), "The value at which the amount of entities currently being ridden will be compared to.", 1)
                    .addExampleObject(new VehicleRidingRecursiveCondition(Optional.empty(), NumberComparator.GREATER_THAN, new StaticValue(1)));
        }
    }

}