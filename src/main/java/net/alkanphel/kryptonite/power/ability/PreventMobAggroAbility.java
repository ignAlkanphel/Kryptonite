package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;

public class PreventMobAggroAbility extends Ability {

    public static final MapCodec<PreventMobAggroAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("mob_conditions", TrueCondition.INSTANCE).forGetter(a -> a.mobConditions),
            Codec.BOOL.optionalFieldOf("mob_aggro_on_attack", true).forGetter(a -> a.mobAggroOnAttack),
            Codec.BOOL.optionalFieldOf("mob_aggro_reset", false).forGetter(a -> a.mobAggroReset),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventMobAggroAbility::new));

    public final Condition mobConditions;
    public final boolean mobAggroOnAttack, mobAggroReset;

    public PreventMobAggroAbility(Condition mobConditions, boolean mobAggroOnAttack, boolean mobAggroReset, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.mobConditions = mobConditions;
        this.mobAggroOnAttack = mobAggroOnAttack;
        this.mobAggroReset = mobAggroReset;
    }

    public boolean doesApply(LivingEntity source, LivingEntity target, AbilityInstance<?> abilityInstance) {
        if (mobAggroOnAttack && source instanceof Mob mob) {
            if (mob.getLastHurtByMob() == target) return false;
        }

        return mobConditions.test(DataContext.forAffected(source, target, abilityInstance));
    }

    public static boolean shouldIgnore(LivingEntity source, LivingEntity target) {
        return AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.PREVENT_MOB_AGGRO.get())
                .stream()
                .anyMatch(instance -> instance.getAbility().doesApply(source, target, instance));
    }

    public static boolean shouldAggroReset(LivingEntity source, LivingEntity target) {
        return AbilityUtil.getEnabledInstances(target, KryptoniteAbilitySerializers.PREVENT_MOB_AGGRO.get())
                .stream()
                .anyMatch(instance -> instance.getAbility().mobAggroReset && instance.getAbility().doesApply(source, target, instance));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_MOB_AGGRO.get();
    }

    public static class Serializer extends AbilitySerializer<PreventMobAggroAbility> {

        @Override
        public MapCodec<PreventMobAggroAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventMobAggroAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Prevent Mob Aggro")
                    .setDescription("Prevents mobs from targeting the entity with this ability. For this ability, the \"source\" is the the entity with this ability & the \"target\" is the mob.\"")
                    .addOptional("entity_conditions", TYPE_CONDITION_LIST, "Condition(s) that the mobs must meet to be affected.")
                    .addOptional("mob_aggro_on_attack", TYPE_BOOLEAN, "If true, mobs that are attacked will be able to fight back.", true)
                    .addOptional("mob_aggro_reset", TYPE_BOOLEAN, "If true, mobs will have their aggro reset.", false)
                    .addExampleObject(new PreventMobAggroAbility(TrueCondition.INSTANCE, true, false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventMobAggroAbility((new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("bee"))))))), false, true, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}