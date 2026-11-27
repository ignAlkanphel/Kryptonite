package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.logic.filter.DamageFilter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class PreventDeathAbility extends Ability {

    public static final MapCodec<PreventDeathAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(ab -> ab.action),
            DamageFilter.CODEC.optionalFieldOf("damage_filters", DamageFilter.EMPTY).forGetter(ab -> ab.damageFilter),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventDeathAbility::new));

    public final List<Action> action;
    public final DamageFilter damageFilter;

    public PreventDeathAbility(List<Action> action, DamageFilter damageFilter, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
        this.damageFilter = damageFilter;
    }

    public boolean doesApply(DamageSource source, float amount) {
        return damageFilter.test(source, amount);
    }

    public void runActions(LivingEntity entity) {
        if (!action.isEmpty()) Action.runList(action, DataContext.forEntity(entity));
    }

    public static boolean tryPreventDeath(LivingEntity entity, DamageSource source, float amount) {
        boolean prevented = false;

        for (AbilityInstance<PreventDeathAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_DEATH.get())) {
            if (!instance.getAbility().doesApply(source, amount)) {
                continue;
            }

            instance.getAbility().runActions(entity);
            prevented = true;
        }

        return prevented;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_DEATH.get();
    }

    public static class Serializer extends AbilitySerializer<PreventDeathAbility> {

        @Override
        public MapCodec<PreventDeathAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventDeathAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Prevents the entity from dying. Damage that would kill reduces health to 1/2 a heart.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed every time a death is prevented.")
                    .addOptional("damage_filters", DamageFilter.documentation(provider), "Damage filter(s) the damage source must pass for death to be prevented.")
                    .addExampleObject(new PreventDeathAbility(List.of(), DamageFilter.EMPTY, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventDeathAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say My death was greatly exaggerated!")))), DamageFilter.ofAttacker(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("iron_golem"))))))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}