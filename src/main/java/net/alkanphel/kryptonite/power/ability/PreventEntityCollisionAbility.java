package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventEntityCollisionAbility extends Ability {

    public static final MapCodec<PreventEntityCollisionAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("entity_conditions", TrueCondition.INSTANCE).forGetter(ab -> ab.entityConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventEntityCollisionAbility::new));

    public final Condition entityConditions;

    public PreventEntityCollisionAbility(Condition entityConditions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityConditions = entityConditions;
    }

    public boolean appliesTo(Entity source, Entity target, AbilityInstance<?> abilityInstance) {
        return entityConditions.test(DataContext.forAffected(target, source, abilityInstance));
    }

    public static boolean doesApply(Entity source, Entity target) {
        boolean sourcePrevents = source instanceof LivingEntity living &&
                AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_ENTITY_COLLISION.get())
                        .stream().anyMatch(instance -> instance.getAbility().appliesTo(source, target, instance));

        boolean targetPrevents = target instanceof LivingEntity living &&
                AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.PREVENT_ENTITY_COLLISION.get())
                        .stream().anyMatch(instance -> instance.getAbility().appliesTo(target, source, instance));

        return sourcePrevents || targetPrevents;
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
            builder.setDescription("Prevents the entity with this ability from colliding with other entities. For this ability, the \"source\" is the entity with this ability & the \"target\" is the entity being collided with.")
                    .addOptional("entity_condition", TYPE_CONDITION_LIST, "Condition(s) the target must meet for collision to be prevented.")
                    .addExampleObject(new PreventEntityCollisionAbility(TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}