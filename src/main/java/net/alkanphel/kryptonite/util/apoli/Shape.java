package net.alkanphel.kryptonite.util.apoli;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public enum Shape implements StringRepresentable {
    CUBE, STAR, SPHERE;

    public static final Codec<Shape> CODEC = StringRepresentable.fromEnum(Shape::values);

    public final Collection<BlockPos> getBlockPositions(BlockPos center, int radius) {
        ObjectOpenHashSet<BlockPos> blockPositions = new ObjectOpenHashSet<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    if (this.getBlockDistance(x, y, z) <= radius) {
                        blockPositions.add(new BlockPos(center.offset(x, y, z)));
                    }
                }
            }
        }

        blockPositions.trim();
        return blockPositions;
    }

    public final Collection<Entity> getEntities(Level level, Vec3 center, double radius) {
        ObjectOpenHashSet<Entity> entities = new ObjectOpenHashSet<>();

        double diameter = radius * 2;
        double x, y, z;

        for (Entity entity : level.getEntitiesOfClass(Entity.class, AABB.ofSize(center, diameter, diameter, diameter))) {
            x = Math.abs(entity.getX() - center.x());
            y = Math.abs(entity.getY() - center.y());
            z = Math.abs(entity.getZ() - center.z());

            if (this.getDistance(x, y, z) <= radius + 1) {
                entities.add(entity);
            }
        }

        entities.trim();
        return entities;
    }

    private double getBlockDistance(int x, int y, int z) {
        return switch (this) {
            case CUBE -> 0;
            case STAR -> Math.abs(x) + Math.abs(y) + Math.abs(z);
            default -> getDistance(x, y, z);
        };
    }

    public double getDistance(double x, double y, double z) {
        return switch (this) {
            case CUBE -> Math.max(Math.max(x, y), z);
            case STAR -> x + y + z;
            case SPHERE -> Math.sqrt(x * x + y * y + z * z);
        };
    }

    public static double getDistance(Shape shape, double x, double y, double z) {
        return shape.getDistance(x, y, z);
    }

    @Override
    public @NotNull String getSerializedName() {
        return name().toLowerCase();
    }

}