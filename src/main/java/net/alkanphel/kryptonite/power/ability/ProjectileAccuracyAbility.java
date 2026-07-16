package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.Collections;
import java.util.List;

public class ProjectileAccuracyAbility extends Ability {

    public static final MapCodec<ProjectileAccuracyAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.CODEC.listOf().optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ProjectileAccuracyAbility::new));

    public final List<BiCondition> biEntityConditions;

    public ProjectileAccuracyAbility(List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityConditions = biEntityConditions;
    }

    public boolean doesApply(LivingEntity holder, Projectile projectile) {
        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, holder, projectile)) {
            return false;
        }

        return true;
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
            builder.setDescription("Makes projectiles shot by the player more accurate by making their divergence less random. In the context of this ability, the \"actor\" is the ability holder & \"target\" the projectile.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only affects projectiles that fulfill these bi conditions.")
                    .addExampleObject(new ProjectileAccuracyAbility(List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("arrow")))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, Collections.emptyList()));
        }
    }

}