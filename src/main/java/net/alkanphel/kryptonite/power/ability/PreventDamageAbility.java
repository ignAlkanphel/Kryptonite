package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.damage.DamageTypeDamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PreventDamageAbility extends Ability {

    public static final MapCodec<PreventDamageAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            Codec.BOOL.optionalFieldOf("prevent_fire", false).forGetter(ab -> ab.preventFire),
            Codec.BOOL.optionalFieldOf("prevent_freeze", false).forGetter(ab -> ab.preventFreeze),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventDamageAbility::new));

    public final List<DamageCondition> damageConditions;
    public final boolean preventFire, preventFreeze;

    public PreventDamageAbility(List<DamageCondition> damageConditions, boolean preventFire, boolean preventFreeze, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.damageConditions = damageConditions;
        this.preventFire = preventFire;
        this.preventFreeze = preventFreeze;

    }

    public static boolean isImmuneAgainst(AbilityInstance<PreventDamageAbility> ability, DamageSource source, float amount, Entity self) {
        if (!ability.isEnabled()) return false;

        var damageConditions = ability.getAbility().damageConditions;

        if (!damageConditions.isEmpty() && !DamageCondition.checkConditions(damageConditions, source, amount)) {
            return false;
        }

        return true;
    }

    public static boolean preventsFire(LivingEntity entity) {
        for (AbilityInstance<PreventDamageAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_DAMAGE.get())) {
            if (instance.getAbility().preventFire) return true;
        }

        return false;
    }

    public static boolean preventsFreeze(LivingEntity entity) {
        for (AbilityInstance<PreventDamageAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_DAMAGE.get())) {
            if (instance.getAbility().preventFreeze) return true;
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
            builder.setName("Prevent Damage")
                    .setDescription("Makes the entity immune to certain damage types.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "The damage conditions that must be fulfilled for damage to be prevented.")
                    .addOptional("prevent_fire", TYPE_BOOLEAN, "If true, makes the entity unable to be set on fire.", false)
                    .addOptional("prevent_freeze", TYPE_BOOLEAN, "If true, makes the entity unable to freeze.", false)
                    .addExampleObject(new PreventDamageAbility(List.of(), false, false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventDamageAbility(List.of(new DamageTypeDamageCondition(provider.lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(DamageTypeTags.IS_EXPLOSION))), false, false, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}