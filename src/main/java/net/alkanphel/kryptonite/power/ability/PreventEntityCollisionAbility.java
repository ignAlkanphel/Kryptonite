package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventEntityCollisionAbility extends Ability {

    public static final MapCodec<PreventEntityCollisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(ab -> ab.biEntityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEntityCollisionAbility::new));

    public final List<BiCondition> biEntityConditions;

    public PreventEntityCollisionAbility(List<BiCondition> biEntityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.biEntityConditions = biEntityConditions;
    }

    public boolean appliesTo(Entity holder, Entity target) {
        return biEntityConditions.isEmpty() || BiCondition.checkConditions(biEntityConditions, holder, target);
    }

    public static boolean doesApply(Entity fromEntity, Entity collidingEntity) {
        boolean fromPrevents = fromEntity instanceof LivingEntity living &&
                AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_ENTITY_COLLISION.get())
                .stream().anyMatch(instance -> instance.getAbility().appliesTo(fromEntity, collidingEntity));

        boolean collidingPrevents = collidingEntity instanceof LivingEntity livingColliding &&
                AbilityUtil.getEnabledInstances(livingColliding, KryptoniteAbilitySerializers.PREVENT_ENTITY_COLLISION.get())
                .stream().anyMatch(instance -> instance.getAbility().appliesTo(collidingEntity, fromEntity));

        return fromPrevents || collidingPrevents;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_ENTITY_COLLISION.get();
    }

    public static class Serializer extends AbilitySerializer<PreventEntityCollisionAbility> {

        @Override
        public MapCodec<PreventEntityCollisionAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventEntityCollisionAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the entity from colliding with other entities. In the context of this ability, the \"actor\" is the ability holder & \"target\" the colliding entity.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only prevents entity collision when these bi condition are fulfilled.")
                    .addExampleObject(new PreventEntityCollisionAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}