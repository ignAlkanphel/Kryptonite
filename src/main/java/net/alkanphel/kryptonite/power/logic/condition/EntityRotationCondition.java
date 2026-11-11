package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Locale;
import java.util.function.Function;

public record EntityRotationCondition(RotationType sourceRotationType, RotationType targetRotationType, EnumSet<Direction.Axis> axes, NumberComparator comparator, Value compareTo) implements Condition {

    public static final MapCodec<EntityRotationCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RotationType.CODEC.optionalFieldOf("source_rotation", RotationType.HEAD).forGetter(EntityRotationCondition::sourceRotationType),
            RotationType.CODEC.optionalFieldOf("target_rotation", RotationType.BODY).forGetter(EntityRotationCondition::targetRotationType),
            KryptoniteCodecs.DIRECTION_AXIS_CODEC.optionalFieldOf("axes", EnumSet.allOf(Direction.Axis.class)).forGetter(EntityRotationCondition::axes),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(EntityRotationCondition::comparator),
            Value.NUMBER_CODEC.fieldOf("compare_to").forGetter(EntityRotationCondition::compareTo)
    ).apply(instance, EntityRotationCondition::new));

    @Override
    public boolean test(DataContext context) {
        Entity source = context.getSource();
        Entity target = context.getTarget();

        if (source == null || target == null) return false;

        Vec3 sourceRotation = sourceRotationType.getRotation(source);
        Vec3 targetRotation = targetRotationType.getRotation(target);

        sourceRotation = reduceAxes(sourceRotation, axes);
        targetRotation = reduceAxes(targetRotation, axes);

        DataContext valueContext = DataContext.forEntity(source).with(DataContextKeys.ENTITY, target);

        return comparator.compare(getAngleBetween(sourceRotation, targetRotation), compareTo.getAsDouble(valueContext));
    }

    private static double getAngleBetween(Vec3 a, Vec3 b) {
        double dot = a.dot(b);
        return dot / (a.length() * b.length());
    }

    private static Vec3 reduceAxes(Vec3 vector, EnumSet<Direction.Axis> axesToKeep) {
        return new Vec3(
                axesToKeep.contains(Direction.Axis.X) ? vector.x : 0,
                axesToKeep.contains(Direction.Axis.Y) ? vector.y : 0,
                axesToKeep.contains(Direction.Axis.Z) ? vector.z : 0
        );
    }

    private static Vec3 getBodyRotationVector(Entity entity) {
        if (!(entity instanceof LivingEntity livingEntity)) {
            return entity.getViewVector(1.0f);
        }

        float f = livingEntity.getXRot() * ((float) Math.PI / 180);
        float g = -livingEntity.getYRot() * ((float) Math.PI / 180);

        float h = Mth.cos(g);
        float i = Mth.sin(g);
        float j = Mth.cos(f);
        float k = Mth.sin(f);

        return new Vec3(i * j, -k, h * j);
    }

    @Override
    public ConditionSerializer<EntityRotationCondition> getSerializer() {
        return KryptoniteConditionSerializers.ENTITY_ROTATION.get();
    }

    public static class Serializer extends ConditionSerializer<EntityRotationCondition> {

        @Override
        public MapCodec<EntityRotationCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, EntityRotationCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Entity Rotation")
                    .setDescription("Compares the rotation angle of the source entity to the target entity.")
                    .addOptional("source_rotation", SettingType.enumList(RotationType.values()), "Determines the initial point of the rotation for the source.", RotationType.HEAD)
                    .addOptional("target_rotation", SettingType.enumList(RotationType.values()), "Determines the initial point of the rotation for the target.", RotationType.BODY)
                    .addOptional("axes", SettingType.enumList(Direction.Axis.values()), "The axes to get the angle values to calculate, and compare to.", EnumSet.allOf(Direction.Axis.class))
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "Determines how the calculated angle value should be compared to the specified value.")
                    .add("compare_to", TYPE_DOUBLE_VALUE, "The value at which the calculated angle value will be compared to.")
                    .addExampleObject(new EntityRotationCondition(RotationType.HEAD, RotationType.BODY, EnumSet.allOf(Direction.Axis.class), NumberComparator.GREATER_OR_EQUAL, new StaticValue(0.0D)));
        }
    }

    public enum RotationType implements StringRepresentable {
        HEAD(e -> e.getViewVector(1.0f)),
        BODY(EntityRotationCondition::getBodyRotationVector);

        public static final Codec<RotationType> CODEC = StringRepresentable.fromEnum(RotationType::values);
        private final Function<Entity, Vec3> function;

        RotationType(Function<Entity, Vec3> function) {
            this.function = function;
        }

        public Vec3 getRotation(Entity entity) {
            return function.apply(entity);
        }

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}