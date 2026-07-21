package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.KryptoniteSettingType;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;

import java.util.List;

public record EntityInRadiusCondition(List<BiCondition> biCondition, Shape shape, NumberComparator comparator, Value compareTo, Value radius) implements Condition {

    public static final MapCodec<EntityInRadiusCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(EntityInRadiusCondition::biCondition),
            Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(EntityInRadiusCondition::shape),
            NumberComparator.CODEC.optionalFieldOf("comparator", NumberComparator.GREATER_THAN).forGetter(EntityInRadiusCondition::comparator),
            Value.CODEC.optionalFieldOf("compare_to", new StaticValue(0)).forGetter(EntityInRadiusCondition::compareTo),
            Value.CODEC.fieldOf("radius").forGetter(EntityInRadiusCondition::radius)
    ).apply(instance, EntityInRadiusCondition::new));

    @Override
    public boolean test(DataContext context) {
        Entity entity = context.getEntity();
        if (entity == null) return false;

        int compareTo = this.compareTo.getAsInt(context);

        int threshold = switch (comparator) {
            case EQUALS, LESS_OR_EQUAL, GREATER_THAN -> compareTo + 1;
            case LESS_THAN, GREATER_OR_EQUAL -> compareTo;
            default -> -1;
        };

        int matches = 0;
        double radius = Math.clamp(this.compareTo.getAsDouble(context), 0.0, Double.MAX_VALUE);

        for (Entity target : shape.getEntities(entity.level(), entity.getPosition(1.0F), radius)) {
            if (biCondition.isEmpty() || BiCondition.checkConditions(biCondition, entity, target)) {
                ++matches;
            }

            if (threshold != -1 && matches == threshold) {
                break;
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
                    .setDescription("Checks the number of entities within a radius. In the context of this ability, the \"actor\" is the entity with this ability & the \"target\" is the entity being counted.")
                    .add("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only target entities that fulfill these conditions will be counted.")
                    .addOptional("shape", KryptoniteDocumented.TYPE_SHAPE, "The shape of the area to search.", Shape.CUBE)
                    .addOptional("comparator", TYPE_NUMBER_COMPARATOR, "The comparison operator being used", NumberComparator.GREATER_THAN)
                    .addOptional("compare_to", TYPE_VALUE, "The value that is being compared against", 0)
                    .add("radius", KryptoniteSettingType.doubleValueRange(0.0, Double.MAX_VALUE), "The radius to search within.")
                    .addExampleObject(new EntityInRadiusCondition(List.of(), Shape.CUBE, NumberComparator.GREATER_THAN, new StaticValue(0), new StaticValue(16)));
        }
    }

}