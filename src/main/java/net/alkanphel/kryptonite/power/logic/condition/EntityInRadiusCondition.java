package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

public record EntityInRadiusCondition(Condition entityConditions, Shape shape, NumberComparator comparator, Value compareTo, Value radius) implements Condition {

    public static final MapCodec<EntityInRadiusCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(EntityInRadiusCondition::entityConditions),
            Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(EntityInRadiusCondition::shape),
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_THAN).forGetter(EntityInRadiusCondition::comparator),
            Value.CODEC.optionalFieldOf("compare_to", new StaticValue(1)).forGetter(EntityInRadiusCondition::compareTo),
            Value.CODEC.fieldOf("radius").forGetter(EntityInRadiusCondition::radius)
    ).apply(instance, EntityInRadiusCondition::new));

    @Override
    public boolean test(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        int compareTo = this.compareTo.getAsInt(context);
        int matches = 0;
        double radius = Math.clamp(this.radius.getAsDouble(context), 0.0, Double.MAX_VALUE);

        for (Entity target : shape.getEntities(entity.level(), entity.getPosition(1.0F), radius)) {
            if (target == entity) continue;

            if (entityConditions.test(DataContext.forAffected(target, entity, null))) {
                ++matches;
            }

            if (comparator.compare(matches, compareTo)) {
                return true;
            }
        }

        return comparator.compare(matches, compareTo);
    }

    @Override
    public ConditionSerializer<EntityInRadiusCondition> getSerializer() {
        return KryptoniteConditionSerializers.ENTITY_IN_RADIUS.get();
    }

    public static class Serializer extends ConditionSerializer<EntityInRadiusCondition> {

        @Override
        public MapCodec<EntityInRadiusCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, EntityInRadiusCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Entity In Radius")
                    .setDescription("Checks the number of entities within a radius. For this condition, the \"source\" is the entity at the center of the search & the \"target\" is each entity being counted.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the target must meet to be counted.")
                    .addOptional("shape", KryptoniteDocumented.TYPE_SHAPE, "Shape of the area to search.", Shape.CUBE)
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "Comparison operator being used", NumberComparator.GREATER_THAN)
                    .addOptional("compare_to", TYPE_INT_VALUE, "Value that is being compared against", 1)
                    .add("radius", KryptoniteSettingType.doubleValueRange(0.0, Double.MAX_VALUE), "The radius to search within.")
                    .addExampleObject(new EntityInRadiusCondition(TrueCondition.INSTANCE, Shape.CUBE, NumberComparator.GREATER_THAN, new StaticValue(0), new StaticValue(16)));
        }
    }

}