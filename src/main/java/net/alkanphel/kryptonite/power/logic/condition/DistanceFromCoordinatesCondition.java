package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.MapCodec;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.alkanphel.kryptonite.util.apoli.ability.DistanceFromCoordinates;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.NumberComparator;

import java.util.Optional;

public record DistanceFromCoordinatesCondition(Reference reference, Shape shape, Optional<Integer> roundToDigit, Optional<Offset> offset, NumberComparator comparator, double compareTo, boolean scaleReferenceToDimension, boolean scaleDistanceToDimension, boolean ignoreX, boolean ignoreY, boolean ignoreZ)
        implements Condition, DistanceFromCoordinates {

    public static final MapCodec<DistanceFromCoordinatesCondition> CODEC = DistanceFromCoordinates.mapCodec(DistanceFromCoordinatesCondition::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, DistanceFromCoordinatesCondition> STREAM_CODEC = DistanceFromCoordinates.streamCodec(DistanceFromCoordinatesCondition::new);

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null) return false;

        return testCondition(Either.right(context));
    }

    @Override
    public ConditionSerializer<DistanceFromCoordinatesCondition> getSerializer() {
        return KryptoniteConditionSerializers.DISTANCE_FROM_COORDINATES.get();
    }

    public static class Serializer extends ConditionSerializer<DistanceFromCoordinatesCondition> {

        @Override
        public MapCodec<DistanceFromCoordinatesCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, DistanceFromCoordinatesCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Distance From Coordinates")
                    .setDescription("Compares the distance of the entity's current position to the specified coordinates.")
                    .addOptional("reference", SettingType.enumList(Reference.values()), "The point to compare the distance to.", Reference.WORLD_ORIGIN)
                    .addOptional("shape", KryptoniteDocumented.TYPE_SHAPE, "Determines the shape of the check.", Shape.CUBE)
                    .addOptional("round_to_digit", TYPE_INT, "If specified, rounds the result to the closest number with the specified amount of digits after the comma. Negative numbers also work (e.g: -2 rounds to multiples of 100).")
                    .addOptional("offset", TYPE_VECTOR3, "If specified, determines how much the reference point should be offset.")
                    .add("comparator", TYPE_NUMBER_COMPARATOR, "Determines how the calculated distance (in blocks) should be compared to the specified value.")
                    .add("compare_to", TYPE_DOUBLE, "The value at which the calculated distance (in blocks) will be compared to.")
                    .addOptional("scale_reference_to_dimension", TYPE_BOOLEAN, "Whether to scale the reference coordinates to the current dimension.", true)
                    .addOptional("scale_distance_to_dimension", TYPE_BOOLEAN, "Whether to scale the calculated distance to the current dimension.", false)
                    .addOptional("ignore_x", TYPE_BOOLEAN, "If to consider the X axis to be 0.", false)
                    .addOptional("ignore_y", TYPE_BOOLEAN, "If to consider the Y axis to be 0.", false)
                    .addOptional("ignore_z", TYPE_BOOLEAN, "If to consider the Z axis to be 0.", false)
                    .addExampleObject(new DistanceFromCoordinatesCondition(Reference.WORLD_ORIGIN, Shape.CUBE, Optional.empty(), Optional.of(new Offset(Optional.of(1024.0D), Optional.empty(), Optional.of(512.0D))), NumberComparator.LESS_THAN, 8.0D, false, false, false, true, false));
        }
    }

}