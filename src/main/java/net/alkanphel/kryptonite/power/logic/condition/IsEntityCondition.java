package net.alkanphel.kryptonite.power.logic.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteConditionSerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.parrot.ShoulderRidingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.entity.projectile.arrow.*;
import net.minecraft.world.entity.raid.Raider;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.ConditionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import org.jetbrains.annotations.NotNull;

public record IsEntityCondition(Type type) implements Condition {

    public static final MapCodec<IsEntityCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Type.CODEC.optionalFieldOf("type", Type.EXISTING).forGetter(IsEntityCondition::type)
    ).apply(instance, IsEntityCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, IsEntityCondition> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(Type.class), IsEntityCondition::type,
            IsEntityCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();

        return switch (type()) {
            case EXISTING -> entity != null;
            case LIVING -> entity instanceof LivingEntity;
            case PROJECTILE -> entity instanceof Projectile;
            case ARROW -> entity instanceof AbstractArrow;
            case MOB -> entity instanceof Mob;
            case NEUTRAL_MOB -> entity instanceof NeutralMob;
            case PATHFINDER_MOB -> entity instanceof PathfinderMob;
            case MONSTER -> entity instanceof Monster;
            case ANIMAL -> entity instanceof Animal;
            case FLYING_ANIMAL -> entity instanceof FlyingAnimal;
            case TAMABLE_ANIMAL -> entity instanceof TamableAnimal || entity instanceof AbstractHorse;
            case AGEABLE_MOB -> entity instanceof AgeableMob;
            case AGEABLE_WATER_CREATURE -> entity instanceof AgeableWaterCreature;
            case SHOULDER_RIDING -> entity instanceof ShoulderRidingEntity;
            case LEASHABLE -> entity instanceof Leashable;
            case BUCKETABLE -> entity instanceof Bucketable;
            case RAIDER -> entity instanceof Raider;
        };
    }

    @Override
    public ConditionSerializer<IsEntityCondition> getSerializer() {
        return KryptoniteConditionSerializers.IS_ENTITY.get();
    }

    public static class Serializer extends ConditionSerializer<IsEntityCondition> {

        @Override
        public MapCodec<IsEntityCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, IsEntityCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Is Entity")
                    .setDescription("Checks what \"instanceof\" type the entity is. \"existing\" simply checks if the entity exists.")
                    .addOptional("type", TYPE_STRING, "The \"instanceof\" type.", Type.EXISTING)
                    .addExampleObject(new IsEntityCondition(Type.EXISTING))
                    .addExampleObject(new IsEntityCondition(Type.LIVING));
        }
    }

    public enum Type implements StringRepresentable {
        EXISTING,
        LIVING,
        PROJECTILE,
        ARROW,
        MOB,
        NEUTRAL_MOB,
        PATHFINDER_MOB,
        MONSTER,
        ANIMAL,
        FLYING_ANIMAL,
        TAMABLE_ANIMAL,
        AGEABLE_MOB,
        AGEABLE_WATER_CREATURE,
        SHOULDER_RIDING,
        LEASHABLE,
        BUCKETABLE,
        RAIDER;

        public static final Codec<Type> CODEC = StringRepresentable.fromEnum(Type::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}