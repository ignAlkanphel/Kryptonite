package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ModifyKnockbackAbility extends Ability {

    public static final MapCodec<ModifyKnockbackAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("strength_modifiers", List.of()).forGetter(a -> a.strengthModifiers),
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("ratio_x_modifiers", List.of()).forGetter(a -> a.ratioXModifiers),
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("ratio_z_modifiers", List.of()).forGetter(a -> a.ratioZModifiers),
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyKnockbackAbility::new));

    public final List<KryptoniteModifiers.ValueModifier> strengthModifiers;
    public final List<KryptoniteModifiers.ValueModifier> ratioXModifiers;
    public final List<KryptoniteModifiers.ValueModifier> ratioZModifiers;
    public final List<Action> entityActions;

    public ModifyKnockbackAbility(List<KryptoniteModifiers.ValueModifier> strengthModifiers, List<KryptoniteModifiers.ValueModifier> ratioXModifiers, List<KryptoniteModifiers.ValueModifier> ratioZModifiers, List<Action> entityActions, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.strengthModifiers = strengthModifiers;
        this.ratioXModifiers = ratioXModifiers;
        this.ratioZModifiers = ratioZModifiers;
        this.entityActions = entityActions;
    }

    public float applyStrength(float value, LivingEntity entity, AbilityInstance<?> instance) {
        return Math.max(0F, KryptoniteModifiers.applyModifiers(value, strengthModifiers, DataContext.forAbility(entity, instance)));
    }

    public double applyRatioX(double value, LivingEntity entity, AbilityInstance<?> instance) {
        return KryptoniteModifiers.applyModifiers((float) value, ratioXModifiers, DataContext.forAbility(entity, instance));
    }

    public double applyRatioZ(double value, LivingEntity entity, AbilityInstance<?> instance) {
        return KryptoniteModifiers.applyModifiers((float) value, ratioZModifiers, DataContext.forAbility(entity, instance));
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.MODIFY_KNOCKBACK.get();
    }

    public static class Serializer extends AbilitySerializer<ModifyKnockbackAbility> {

        @Override
        public MapCodec<ModifyKnockbackAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ModifyKnockbackAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Modify Knockback")
                    .setDescription("Modifies the incoming knockback for the entity that has this ability.")
                    .add("strength_modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers applied to knockback strength.")
                    .add("ratio_x_modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers applied to knockback X ratio.")
                    .add("ratio_z_modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers applied to knockback Z ratio.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run upon being knocked back.")
                    .addExampleObject(new ModifyKnockbackAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-1), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyKnockbackAbility(List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0.75), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}