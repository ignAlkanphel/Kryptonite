package net.alkanphel.kryptonite.util.player;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.KryptoniteAttachments;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ChunkResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public record LastLocation(ResourceKey<Level> dimension, double x, double y, double z, float yaw, float pitch) {

    private static final Set<ResourceKey<Level>> EXCLUDED_DIMENSIONS = new HashSet<>();
    private static final Set<ResourceKey<DimensionType>> EXCLUDED_DIMENSION_TYPES = new HashSet<>();

    public static void registerExcludedDimension(ResourceKey<Level> dimension) {
        EXCLUDED_DIMENSIONS.add(dimension);
    }

    public static void registerExcludedDimensionType(ResourceKey<DimensionType> dimensionType) {
        EXCLUDED_DIMENSION_TYPES.add(dimensionType);
    }

    public static final MapCodec<LastLocation> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(LastLocation::dimension),
            Codec.DOUBLE.fieldOf("x").forGetter(LastLocation::x),
            Codec.DOUBLE.fieldOf("y").forGetter(LastLocation::y),
            Codec.DOUBLE.fieldOf("z").forGetter(LastLocation::z),
            Codec.FLOAT.fieldOf("yaw").forGetter(LastLocation::yaw),
            Codec.FLOAT.fieldOf("pitch").forGetter(LastLocation::pitch)
    ).apply(inst, LastLocation::new));

    public static final StreamCodec<ByteBuf, LastLocation> STREAM_CODEC = StreamCodec.composite(
            ResourceKey.streamCodec(Registries.DIMENSION), LastLocation::dimension,
            ByteBufCodecs.DOUBLE, LastLocation::x,
            ByteBufCodecs.DOUBLE, LastLocation::y,
            ByteBufCodecs.DOUBLE, LastLocation::z,
            ByteBufCodecs.FLOAT, LastLocation::yaw,
            ByteBufCodecs.FLOAT, LastLocation::pitch,
            LastLocation::new
    );

    public LastLocation {
        if (EXCLUDED_DIMENSIONS.contains(dimension)) {
            throw new IllegalArgumentException("LastLocation cannot be in dimensions: " + dimension.identifier());
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onEntityTeleport(EntityTeleportEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.isCanceled()) return;

        LastLocation.saveLastLocation(player);
    }

    public static boolean saveLastLocation(ServerPlayer player) {
        ResourceKey<DimensionType> key = player.level().dimensionTypeRegistration().getKey();
        if (key != null && EXCLUDED_DIMENSION_TYPES.contains(key)) return false;

        player.setData(KryptoniteAttachments.LAST_LOCATION, Optional.of(new LastLocation(player.level().dimension(), player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot())));
        return true;
    }

    public static boolean wipeLastLocation(ServerPlayer player) {
        boolean hadLocation = player.getData(KryptoniteAttachments.LAST_LOCATION).isPresent();

        player.setData(KryptoniteAttachments.LAST_LOCATION, Optional.empty());
        return hadLocation;
    }

    public static boolean teleportToLastLocation(ServerPlayer player, boolean resetFallDistance, boolean keepRotation, boolean keepMount) {
        Optional<LastLocation> attachment = player.getData(KryptoniteAttachments.LAST_LOCATION);
        if (attachment.isEmpty()) return false;

        LastLocation location = attachment.get();
        ServerLevel level = player.level().getServer().getLevel(location.dimension());
        if (level == null) return false;

        Vec3 pos = new Vec3(location.x(), location.y(), location.z());
        float yaw = keepRotation ? player.getYRot() : location.yaw();
        float pitch = keepRotation ? player.getXRot() : location.pitch();
        if (resetFallDistance) player.resetFallDistance();

        Entity target = keepMount ? player.getRootVehicle() : player;
        return target.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), yaw, pitch, true);
    }

    public static boolean teleportToLastLocationAsync(ServerPlayer player, boolean resetFallDistance, boolean keepRotation, boolean keepMount, @Nullable Runnable onSuccess, @Nullable Runnable onFail) {
        Optional<LastLocation> attachment = player.getData(KryptoniteAttachments.LAST_LOCATION);

        if (attachment.isEmpty()) {
            if (onFail != null) onFail.run();
            return false;
        }

        LastLocation location = attachment.get();
        ServerLevel level = player.level().getServer().getLevel(location.dimension());

        if (level == null) {
            if (onFail != null) onFail.run();
            return false;
        }

        Vec3 pos = new Vec3(location.x(), location.y(), location.z());
        int chunkX = SectionPos.blockToSectionCoord(pos.x);
        int chunkZ = SectionPos.blockToSectionCoord(pos.z);

        loadChunkAsync(level, chunkX, chunkZ).thenAccept(chunkResult -> {
            if (!chunkResult.isSuccess()) {
                if (onFail != null) level.getServer().execute(onFail);
                return;
            }

            level.getServer().execute(() -> {
                Entity target = keepMount ? player.getRootVehicle() : player;
                if (resetFallDistance) target.resetFallDistance();

                float yaw = keepRotation ? player.getYRot() : location.yaw();
                float pitch = keepRotation ? player.getXRot() : location.pitch();

                if (target.teleportTo(level, pos.x, pos.y, pos.z, Set.of(), yaw, pitch, true)) {
                    if (onSuccess != null) onSuccess.run();
                } else {
                    if (onFail != null) onFail.run();
                }
            });
        });

        return true;
    }

    public static CompletableFuture<ChunkResult<ChunkAccess>> loadChunkAsync(ServerLevel level, int x, int z) {
        return level.getChunkSource().getChunkFuture(x, z, ChunkStatus.FULL, true);
    }

}