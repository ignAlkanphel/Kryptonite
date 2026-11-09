package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;

public class ProjectileAccuracyAbility extends Ability {

    public static final MapCodec<ProjectileAccuracyAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(a -> a.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ProjectileAccuracyAbility::new));

    public final Condition entityConditions;

    public ProjectileAccuracyAbility(Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityConditions = entityConditions;
    }

    public boolean doesApply(LivingEntity source, Projectile target, AbilityInstance<?> abilityInstance) {
        return entityConditions.test(DataContext.forAffected(target, source, abilityInstance));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PROJECTILE_ACCURACY.get();
    }

    public static class Serializer extends AbilitySerializer<ProjectileAccuracyAbility> {

        @Override
        public MapCodec<ProjectileAccuracyAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ProjectileAccuracyAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Makes projectiles shot by the player more accurate by making their divergence less random. For this ability, the \"source\" is the entity that has this ability & the \"target\" is the projectile.")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the projectile must meet to be affected.")
                    .addExampleObject(new ProjectileAccuracyAbility(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("arrow")))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}