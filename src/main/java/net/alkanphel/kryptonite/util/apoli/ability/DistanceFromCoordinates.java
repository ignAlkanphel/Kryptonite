package net.alkanphel.kryptonite.util.apoli.ability;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.alkanphel.kryptonite.util.apoli.Shape;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.NumberComparator;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

/**
 *	@author Alluysl
 * 	@author (refactored by) eggohito
 *  @author (ported to Palladium by) Alkanphel
 */
public interface DistanceFromCoordinates {

    Reference reference();
    Shape shape();

    Optional<Integer> roundToDigit();
    Optional<Offset> offset();

    NumberComparator comparator();
    double compareTo();

    boolean scaleReferenceToDimension();
    boolean scaleDistanceToDimension();

    boolean ignoreX();
    boolean ignoreY();
    boolean ignoreZ();

    static <T extends DistanceFromCoordinates> MapCodec<T> mapCodec(Constructor<T> constructor) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Reference.CODEC.optionalFieldOf("reference", Reference.WORLD_ORIGIN).forGetter(DistanceFromCoordinates::reference),
                Shape.CODEC.optionalFieldOf("shape", Shape.CUBE).forGetter(DistanceFromCoordinates::shape),
                Codec.INT.optionalFieldOf("round_to_digit").forGetter(DistanceFromCoordinates::roundToDigit),
                Offset.CODEC.optionalFieldOf("offset").forGetter(DistanceFromCoordinates::offset),
                NumberComparator.CODEC.fieldOf("comparator").forGetter(DistanceFromCoordinates::comparator),
                Codec.DOUBLE.fieldOf("compare_to").forGetter(DistanceFromCoordinates::compareTo),
                Codec.BOOL.optionalFieldOf("scale_reference_to_dimension", true).forGetter(DistanceFromCoordinates::scaleReferenceToDimension),
                Codec.BOOL.optionalFieldOf("scale_distance_to_dimension", false).forGetter(DistanceFromCoordinates::scaleDistanceToDimension),
                Codec.BOOL.optionalFieldOf("ignore_x", false).forGetter(DistanceFromCoordinates::ignoreX),
                Codec.BOOL.optionalFieldOf("ignore_y", false).forGetter(DistanceFromCoordinates::ignoreY),
                Codec.BOOL.optionalFieldOf("ignore_z", false).forGetter(DistanceFromCoordinates::ignoreZ)
        ).apply(instance, constructor::create));
    }

    static <T extends DistanceFromCoordinates> StreamCodec<RegistryFriendlyByteBuf, T> streamCodec(Constructor<T> constructor) {
        return StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(Reference.class), DistanceFromCoordinates::reference,
                NeoForgeStreamCodecs.enumCodec(Shape.class), DistanceFromCoordinates::shape,
                ByteBufCodecs.optional(ByteBufCodecs.INT), DistanceFromCoordinates::roundToDigit,
                ByteBufCodecs.optional(Offset.STREAM_CODEC), DistanceFromCoordinates::offset,
                NumberComparator.STREAM_CODEC, DistanceFromCoordinates::comparator,
                ByteBufCodecs.DOUBLE, DistanceFromCoordinates::compareTo,
                ByteBufCodecs.BOOL, DistanceFromCoordinates::scaleReferenceToDimension,
                ByteBufCodecs.BOOL, DistanceFromCoordinates::scaleDistanceToDimension,
                ByteBufCodecs.BOOL, DistanceFromCoordinates::ignoreX,
                ByteBufCodecs.BOOL, DistanceFromCoordinates::ignoreY,
                ByteBufCodecs.BOOL, DistanceFromCoordinates::ignoreZ,
                constructor::create
        );
    }

    @FunctionalInterface
    interface Constructor<T> {
        T create(Reference reference, Shape shape, Optional<Integer> roundToDigit, Optional<Offset> offset, NumberComparator comparator, double compareTo, boolean scaleReferenceToDimension, boolean scaleDistanceToDimension, boolean ignoreX, boolean ignoreY, boolean ignoreZ);
    }

    default boolean testCondition(Either<BlockConditionContext, DataContext> context) {
        Level level = context.map(BlockConditionContext::level, DataContext::getLevel);
        BlockPos pos = context.map(BlockConditionContext::pos, DataContext::getBlockPos);

        double coordinateScale = level.dimensionType().coordinateScale();

        double x = 0;
        double y = 0;
        double z = 0;

        //	Query the reference's scaled coordinates
        switch (reference()) {
//			case PLAYER_SPAWN, PLAYER_NATURAL_SPAWN -> {
            // 	These references are not yet implemented
//			}
            case WORLD_SPAWN -> {

                //	This, and other of it parts, has been commented since other dimensions (or worlds, in Yarn's terms) can have
                //	its own spawn points (which can be set via `/setworldspawn`)	-eggohito

//				if (resultOnWrongDimension().isPresent() && world.getRegistryKey != World.OVERWORLD) {
//					return resultOnWrongDimension().get();
//				}

                BlockPos spawnPos = level.getRespawnData().globalPos().pos();

                x = spawnPos.getX();
                y = spawnPos.getY();
                z = spawnPos.getZ();

            }
            case WORLD_ORIGIN -> {
                //	The origin of a world is at 0, 0, 0, so we don't need to do anything at this point
            }
        }

        Offset off = offset().orElse(null);
        if (off != null) {
            x += off.x().orElse(0.0);
            y += off.y().orElse(0.0);
            z += off.z().orElse(0.0);
        }

        if (scaleReferenceToDimension() && (x != 0 || z != 0)) {

            //	Upon further investigation, a dimension cannot have a coordinate scale of absolute 0 as its value is bound from
            //	1.0E-5F (0.00001) to 3.0E7 (30000000), meaning that this section may be unnecessary?	-eggohito

            //	Pocket dimensions?
//			if (coordinateScale == 0) {
            //	A coordinate scale of 0 means that it takes 0 blocks to travel from the overworld to travel 1 block in the dimension,
            //	so the dimension is folded on 0, 0, so unless the overworld reference is at 0, 0, it gets scaled to infinity
//				return outOfBounds(comparison);
//			}

            x /= coordinateScale;
            z /= coordinateScale;

        }

        double xDistance = ignoreX() ? 0 : Math.abs(pos.getX() - x);
        double yDistance = ignoreY() ? 0 : Math.abs(pos.getY() - y);
        double zDistance = ignoreZ() ? 0 : Math.abs(pos.getZ() - z);

        if (scaleDistanceToDimension()) {
            xDistance *= coordinateScale;
            zDistance *= coordinateScale;
        }

        double distance = shape().getDistance(xDistance, yDistance, zDistance);
        double scaledDistance = roundToDigit()
                .map(scale -> new BigDecimal(distance).setScale(scale, RoundingMode.HALF_UP).doubleValue())
                .orElse(distance);

        return comparator().compare(scaledDistance, compareTo());
    }

    record Offset(Optional<Double> x, Optional<Double> y, Optional<Double> z) {

        public static final Codec<Offset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("x").forGetter(Offset::x),
                Codec.DOUBLE.optionalFieldOf("y").forGetter(Offset::y),
                Codec.DOUBLE.optionalFieldOf("z").forGetter(Offset::z)
        ).apply(instance, Offset::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Offset> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), Offset::x,
                ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), Offset::y,
                ByteBufCodecs.optional(ByteBufCodecs.DOUBLE), Offset::z,
                Offset::new
        );
    }

    enum Reference implements StringRepresentable {
        WORLD_SPAWN("world_spawn"),
        WORLD_ORIGIN("world_origin");

        public static final Codec<Reference> CODEC = StringRepresentable.fromEnum(Reference::values);
        private final String name;

        Reference(String name) {
            this.name = name;
        }

        @Override
        public @NotNull String getSerializedName() {
            return name;
        }
    }

}