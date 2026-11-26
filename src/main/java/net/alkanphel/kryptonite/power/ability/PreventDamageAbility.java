package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.DamageFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;

import java.util.List;

public class PreventDamageAbility extends Ability {

    public static final MapCodec<PreventDamageAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageFilter.CODEC.optionalFieldOf("damage_filters", DamageFilter.EMPTY).forGetter(a -> a.damageFilter),
            BooleanValueHolder.CODEC.optionalFieldOf("prevent_fire", BooleanValueHolder.FALSE).forGetter(a -> a.preventFire),
            BooleanValueHolder.CODEC.optionalFieldOf("prevent_freeze", BooleanValueHolder.FALSE).forGetter(a -> a.preventFreeze),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventDamageAbility::new));

    public final DamageFilter damageFilter;
    public final BooleanValueHolder preventFire, preventFreeze;

    public PreventDamageAbility(DamageFilter damageFilter, BooleanValueHolder preventFire, BooleanValueHolder preventFreeze, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.damageFilter = damageFilter;
        this.preventFire = preventFire;
        this.preventFreeze = preventFreeze;
    }

    public static boolean isImmuneAgainst(AbilityInstance<PreventDamageAbility> ability, DamageSource source, float amount, Entity self) {
        if (!ability.isEnabled()) return false;
        return ability.getAbility().damageFilter.test(source, amount);
    }

    public static boolean preventsFire(LivingEntity entity) {
        for (AbilityInstance<PreventDamageAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_DAMAGE.get())) {
            if (instance.getAbility().preventFire.get(DataContext.forAbility(entity, instance))) {
                return true;
            }
        }

        return false;
    }

    public static boolean preventsFreeze(LivingEntity entity) {
        for (AbilityInstance<PreventDamageAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_DAMAGE.get())) {
            if (instance.getAbility().preventFreeze.get(DataContext.forAbility(entity, instance))) {
                return true;
            }
        }

        return false;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_DAMAGE.get();
    }

    public static class Serializer extends AbilitySerializer<PreventDamageAbility> {

        @Override
        public MapCodec<PreventDamageAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventDamageAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Makes the entity immune to certain damage types.")
                    .addOptional("damage_filters", DamageFilter.documentation(provider), "Damage filter(s) the damage source must pass for damage to be prevented.")
                    .addOptional("prevent_fire", TYPE_BOOLEAN_VALUE, "If true, makes the entity unable to be set on fire.", false)
                    .addOptional("prevent_freeze", TYPE_BOOLEAN_VALUE, "If true, makes the entity unable to freeze.", false)
                    .addExampleObject(new PreventDamageAbility(DamageFilter.EMPTY, BooleanValueHolder.FALSE, BooleanValueHolder.FALSE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventDamageAbility(DamageFilter.ofDamageTypes(PalladiumHolderSet.direct(provider.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_EXPLOSION))), BooleanValueHolder.FALSE, BooleanValueHolder.FALSE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}