package net.alkanphel.kryptonite.power.logic.condition.bi;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.BiConditionContext;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.context.DataContextKeys;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import net.threetag.palladium.util.NumberComparator;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;

public record RelativeRotationBiCondition(RotationType actorRotationType, RotationType targetRotationType, EnumSet<Direction.Axis> axes, NumberComparator comparator, Value compareTo) implements BiCondition {

    //TODO Clean up the Axis CODEC
    public static final MapCodec<RelativeRotationBiCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RotationType.CODEC.optionalFieldOf("actor_rotation", RotationType.HEAD).forGetter(RelativeRotationBiCondition::actorRotationType),
            RotationType.CODEC.optionalFieldOf("target_rotation", RotationType.BODY).forGetter(RelativeRotationBiCondition::targetRotationType),
            Direction.Axis.CODEC.listOf().optionalFieldOf("axes", List.of(Direction.Axis.values()))
                    .xmap(list -> EnumSet.copyOf(list.isEmpty() ? EnumSet.allOf(Direction.Axis.class) : list), List::copyOf)
                    .forGetter(RelativeRotationBiCondition::axes),
            NumberComparator.CODEC.fieldOf("comparator").forGetter(RelativeRotationBiCondition::comparator),
            Value.CODEC.fieldOf("compare_to").forGetter(RelativeRotationBiCondition::compareTo)
    ).apply(instance, RelativeRotationBiCondition::new));

    @Override
    public boolean test(BiConditionContext context) {
        if (context.actor() == null || context.target() == null) return false;

        Vec3 actorRotation = actorRotationType.getRotation(context.actor());
        Vec3 targetRotation = targetRotationType.getRotation(context.target());

        actorRotation = reduceAxes(actorRotation, axes);
        targetRotation = reduceAxes(targetRotation, axes);

        var dataContext = DataContext.forEntity(context.actor()).with(DataContextKeys.ENTITY, context.target());

        return comparator.compare(getAngleBetween(actorRotation, targetRotation), compareTo.getAsDouble(dataContext));
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
    public BiConditionSerializer<RelativeRotationBiCondition> getSerializer() {
        return BiConditionSerializers.RELATIVE_ROTATION.get();
    }

    public static class Serializer extends BiConditionSerializer<RelativeRotationBiCondition> {

        @Override
        public MapCodec<RelativeRotationBiCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<BiCondition, RelativeRotationBiCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Relative Rotation")
                    .setDescription("Compares the rotation angle of the actor entity to the target entity.")
                    .addOptional("actor_rotation", SettingType.enumList(RotationType.values()), "Determines the initial point of the rotation for the actor.", RotationType.HEAD)
                    .addOptional("target_rotation", SettingType.enumList(RotationType.values()), "Determines the initial point of the rotation for the target.", RotationType.BODY)
                    .addOptional("axes", SettingType.enumList(Direction.Axis.values()), "The axes to get the angle values to calculate, and compare to.", Direction.Axis.values())
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "Determines how the calculated angle value should be compared to the specified value.")
                    .add("compare_to", TYPE_VALUE, "The value at which the calculated angle value will be compared to.")
                    .addExampleObject(new RelativeRotationBiCondition(RotationType.HEAD, RotationType.BODY, EnumSet.allOf(Direction.Axis.class), NumberComparator.GREATER_OR_EQUAL, new StaticValue(0.0D)));
        }
    }

    public enum RotationType implements StringRepresentable {
        HEAD(e -> e.getViewVector(1.0f)),
        BODY(RelativeRotationBiCondition::getBodyRotationVector);

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
            return name().toLowerCase();
        }
    }

}