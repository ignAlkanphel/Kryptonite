package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
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
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;
import java.util.Optional;

public class PreventMobAggroAbility extends Ability {

    public static final MapCodec<PreventMobAggroAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Condition.CODEC.optionalFieldOf("mob_conditions").forGetter(a -> a.mobConditions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            Codec.BOOL.optionalFieldOf("mob_aggro_on_attack", true).forGetter(a -> a.mobAggroOnAttack),
            Codec.BOOL.optionalFieldOf("mob_aggro_reset", false).forGetter(a -> a.mobAggroReset),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventMobAggroAbility::new));

    public final Optional<Condition> mobConditions;
    public final List<BiCondition> biEntityConditions;
    public final boolean mobAggroOnAttack;
    public final boolean mobAggroReset;

    public PreventMobAggroAbility(Optional<Condition> mobConditions, List<BiCondition> biEntityConditions, boolean mobAggroOnAttack, boolean mobAggroReset, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.mobConditions = mobConditions;
        this.biEntityConditions = biEntityConditions;
        this.mobAggroOnAttack = mobAggroOnAttack;
        this.mobAggroReset = mobAggroReset;
    }

    public boolean doesApply(LivingEntity holder, LivingEntity mob) {
        if (mobAggroOnAttack && mob instanceof Mob m) {
            if (m.getLastHurtByMob() == holder) return false;
        }

        if (mobConditions.isPresent() && !mobConditions.get().test(DataContext.forEntity(mob))) {
            return false;
        }

        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, mob, holder)) {
            return false;
        }

        return true;
    }

    public static boolean shouldIgnore(LivingEntity holder, LivingEntity mob) {
        return AbilityUtil.getEnabledInstances(holder, KryptoniteAbilitySerializers.PREVENT_MOB_AGGRO.get())
                .stream()
                .anyMatch(instance -> instance.getAbility().doesApply(holder, mob));
    }

    public static boolean shouldAggroReset(LivingEntity holder, LivingEntity mob) {
        return AbilityUtil.getEnabledInstances(holder, KryptoniteAbilitySerializers.PREVENT_MOB_AGGRO.get())
                .stream()
                .anyMatch(instance -> instance.getAbility().mobAggroReset && instance.getAbility().doesApply(holder, mob));
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
                    .setDescription("Makes entities that are an instanceof Mob not aggro the entity that has this ability. In the context of this ability, the \"actor\" is the mob & the \"target\" ability holder.")
                    .addOptional("mob_conditions", TYPE_CONDITION_LIST, "If specified, only mobs fulfilling these conditions will be affected.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only applies when fulfilling these bi conditions.")
                    .addOptional("mob_aggro_on_attack", TYPE_BOOLEAN, "If true, mobs that are attacked will be able to fight back.", true)
                    .addOptional("mob_aggro_reset", TYPE_BOOLEAN, "If true, mobs will have their aggro reset.", false)
                    .addExampleObject(new PreventMobAggroAbility(Optional.empty(), List.of(), true, false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventMobAggroAbility(Optional.of(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("bee"))))))), List.of(), false, true, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}