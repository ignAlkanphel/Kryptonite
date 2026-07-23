package net.alkanphel.kryptonite.util.apoli;

import net.alkanphel.kryptonite.power.logic.context.BlockConditionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ExplosionParticleInfo;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class MiscUtil {

    public static final WeightedList<ExplosionParticleInfo> DEFAULT_EXPLOSION_BLOCK_PARTICLES = WeightedList.<ExplosionParticleInfo>builder().add(new ExplosionParticleInfo(ParticleTypes.POOF, 0.5F, 1.0F)).add(new ExplosionParticleInfo(ParticleTypes.SMOKE, 1.0F, 1.0F)).build();

    public static void createExplosion(Level level, Vec3 pos, float power, boolean createFire, Level.ExplosionInteraction interaction, ExplosionDamageCalculator damageCalculator) {
        createExplosion(level, null, pos, power, createFire, interaction, damageCalculator);
    }

    public static void createExplosion(Level level, Entity entity, Vec3 pos, float power, boolean createFire, Level.ExplosionInteraction interaction, ExplosionDamageCalculator damageCalculator) {
        createExplosion(level, entity, null, pos.x(), pos.y(), pos.z(), power, createFire, interaction, damageCalculator);
    }

    public static void createExplosion(Level level, @Nullable Entity entity, @Nullable DamageSource damageSource, double x, double y, double z, float power, boolean createFire, Level.ExplosionInteraction interaction, @Nullable ExplosionDamageCalculator damageCalculator) {
        level.explode(entity, damageSource, damageCalculator, x, y, z, power, createFire, interaction, ParticleTypes.EXPLOSION, ParticleTypes.EXPLOSION_EMITTER, DEFAULT_EXPLOSION_BLOCK_PARTICLES, SoundEvents.GENERIC_EXPLODE);
    }

    @Nullable
    public static ExplosionDamageCalculator getExplosionDamageCalculator(Level level, float indestructibleResistance, @Nullable Predicate<BlockInWorld> indestructibleCondition) {
        return indestructibleCondition == null ? null : new ExplosionDamageCalculator() {

            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState block, FluidState fluid) {
                BlockInWorld blockInWorld = new BlockInWorld((LevelReader) level, pos, true);

                Optional<Float> defaultValue = super.getBlockExplosionResistance(explosion, level, pos, block, fluid);
                Optional<Float> newValue = indestructibleCondition.test(blockInWorld) ? Optional.of(indestructibleResistance) : Optional.empty();

                return defaultValue.isPresent() ? (newValue.isPresent() ? (defaultValue.get() > newValue.get() ? (defaultValue) : newValue) : defaultValue) : defaultValue;
            }

            @Override
            public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
                return !indestructibleCondition.test(new BlockInWorld((LevelReader) level, pos, true));
            }
        };
    }

    @Nullable
    public static ExplosionDamageCalculator createExplosionDamageCalculator(@Nullable Predicate<BlockConditionContext> indestructibleCondition, float resistance) {
        return indestructibleCondition == null ? null : new ExplosionDamageCalculator() {

            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion explosion, BlockGetter level, BlockPos pos, BlockState block, FluidState fluid) {
                Optional<Float> defaultValue = super.getBlockExplosionResistance(explosion, level, pos, block, fluid);
                Optional<Float> newValue = indestructibleCondition.test(new BlockConditionContext((Level) level, pos)) ? Optional.of(resistance) : Optional.empty();

                return defaultValue.flatMap(defVal -> newValue.map(newVal -> defVal > newVal ? defVal : newVal));
            }

            @Override
            public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
                return !indestructibleCondition.test(new BlockConditionContext((Level) level, pos));
            }
        };
    }


    // ------------------------------------------------------------------------------------------------------------------------


    public static Optional<Entity> getEntityWithPassengers(Level level, EntityType<?> entityType, @Nullable CompoundTag entityNbt, Vec3 pos, float yaw, float pitch) {
        return getEntityWithPassengers(level, entityType, entityNbt, pos, Optional.of(yaw), Optional.of(pitch));
    }

    public static Optional<Entity> getEntityWithPassengers(Level level, EntityType<?> entityType, @Nullable CompoundTag entityNbt, Vec3 pos, Optional<Float> yaw, Optional<Float> pitch) {
        if (!(level instanceof ServerLevel serverLevel)) return Optional.empty();

        CompoundTag entityToSpawnNbt = new CompoundTag();
        if (entityNbt != null && !entityNbt.isEmpty()) {
            entityToSpawnNbt.merge(entityNbt);
        }

        entityToSpawnNbt.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
        Entity entityToSpawn = EntityType.loadEntityRecursive(
                entityToSpawnNbt,
                serverLevel,
                EntitySpawnReason.COMMAND,
                entity -> {
                    entity.snapTo(pos.x, pos.y, pos.z, yaw.orElse(entity.getYRot()), pitch.orElse(entity.getXRot()));
                    return entity;
                }
        );

        if (entityToSpawn == null) return Optional.empty();

        if ((entityNbt == null || entityNbt.isEmpty()) && entityToSpawn instanceof Mob mobToSpawn) {
            mobToSpawn.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(BlockPos.containing(pos)), EntitySpawnReason.COMMAND, null);
        }

        return Optional.of(entityToSpawn);
    }

    public static @Nullable Entity getEntityByUuid(@Nullable UUID uuid, @Nullable MinecraftServer server) {
        if (uuid == null || server == null) return null;

        Entity entity;
        for (ServerLevel serverLevel : server.getAllLevels()) {
            if ((entity = serverLevel.getEntity(uuid)) != null) {
                return entity;
            }
        }

        return null;
    }


    public static Vec3 getPoseDependentEyePos(Entity entity) {
        return new Vec3(entity.getX(), entity.getY() + entity.getEyeHeight(entity.getPose()), entity.getZ());
    }

    public static double getAttributeValueOrElse(Entity entity, Holder<Attribute> attribute, double defaultValue) {
        if (entity instanceof LivingEntity livingEntity && livingEntity.getAttributes().hasAttribute(attribute)) {
            return livingEntity.getAttributeValue(attribute);
        }

        else {
            return defaultValue;
        }
    }


    // ------------------------------------------------------------------------------------------------------------------------


    public static boolean hasLocalVanillaRespawnPosition(ServerPlayer player) {
        return player.getRespawnConfig() != null;
    }

    public static void clearLocalVanillaRespawnPosition(ServerPlayer player, boolean showMessage) {
        player.setRespawnPosition(null, showMessage);
    }

    public static boolean teleportToLocalVanillaRespawnPosition(ServerPlayer player, boolean useCharge, boolean keepRotation) {
        if (player.getRespawnConfig() == null) return false;

        TeleportTransition transition = player.findRespawnPositionAndUseSpawnBlock(useCharge, TeleportTransition.DO_NOTHING);
        if (transition.missingRespawnBlock()) return false;

        if (keepRotation) transition = transition.withRotation(player.getYRot(), player.getXRot());
        player.teleport(transition);
        return true;
    }

    public static boolean teleportToGlobalVanillaRespawnPosition(ServerPlayer player, boolean keepRotation) {
        TeleportTransition transition = TeleportTransition.createDefault(player, TeleportTransition.DO_NOTHING);

        if (keepRotation) transition = transition.withRotation(player.getYRot(), player.getXRot());
        player.teleport(transition);
        return true;
    }


    // ------------------------------------------------------------------------------------------------------------------------


    public static <T> Predicate<T> combineOr(Predicate<T> a, Predicate<T> b) {
        if(a == null) return b;
        if(b == null) return a;
        return a.or(b);
    }

    public static <T> Predicate<T> combineAnd(Predicate<T> a, Predicate<T> b) {
        if(a == null) return b;
        if(b == null) return a;
        return a.and(b);
    }

}