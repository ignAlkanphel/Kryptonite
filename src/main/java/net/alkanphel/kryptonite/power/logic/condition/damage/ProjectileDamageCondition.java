package net.alkanphel.kryptonite.power.logic.condition.damage;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializer;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageConditionSerializers;
import net.alkanphel.kryptonite.power.logic.context.DamageConditionContext;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Optional;

public record ProjectileDamageCondition(Optional<HolderSet<EntityType<?>>> projectile, Optional<Condition> projectileCondition) implements DamageCondition {

    public static final MapCodec<ProjectileDamageCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            RegistryCodecs.homogeneousList(Registries.ENTITY_TYPE).optionalFieldOf("projectile").forGetter(ProjectileDamageCondition::projectile),
            Condition.CODEC.optionalFieldOf("projectile_conditions").forGetter(ProjectileDamageCondition::projectileCondition)
    ).apply(instance, ProjectileDamageCondition::new));

    @Override
    public boolean test(DamageConditionContext context) {
        DamageSource damageSource = context.source();
        Entity entitySource = damageSource.getDirectEntity();

        return damageSource.is(DamageTypeTags.IS_PROJECTILE)
                && entitySource != null
                && projectile.map(set -> set.contains(entitySource.getType().builtInRegistryHolder())).orElse(true)
                && projectileCondition.map(condition -> condition.test(DataContext.forEntity(entitySource))).orElse(true);
    }

    @Override
    public DamageConditionSerializer<ProjectileDamageCondition> getSerializer() {
        return DamageConditionSerializers.PROJECTILE.get();
    }

    public static class Serializer extends DamageConditionSerializer<ProjectileDamageCondition> {

        @Override
        public MapCodec<ProjectileDamageCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DamageCondition, ProjectileDamageCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Projectile")
                    .setDescription("Checks if the damage source was projectile damage, and optionally the type of projectile it was (if specified).")
                    .addOptional("projectile", TYPE_ENTITY_TYPE_HOLDER_SET, "If set, the check will only pass if the projectile was of the specified entity type.")
                    .addOptional("projectile_conditions", TYPE_CONDITION_LIST, "If set, the check will only pass if the projectile entity fulfills these conditions.")
                    .addExampleObject(new ProjectileDamageCondition(Optional.empty(), Optional.empty()));
        }
    }

}