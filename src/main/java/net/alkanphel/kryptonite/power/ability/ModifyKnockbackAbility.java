package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.util.KryptoniteModifiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.fml.common.EventBusSubscriber;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class ModifyKnockbackAbility extends Ability {

    public static final MapCodec<ModifyKnockbackAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ApplyTo.CODEC.optionalFieldOf("apply_to", ApplyTo.OTHER).forGetter(a -> a.applyTo),
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("strength_modifiers", List.of()).forGetter(a -> a.strengthModifiers),
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("ratio_x_modifiers", List.of()).forGetter(a -> a.ratioXModifiers),
            KryptoniteModifiers.VALUE_MODIFIERS_CODEC.optionalFieldOf("ratio_z_modifiers", List.of()).forGetter(a -> a.ratioZModifiers),
            Action.LIST_CODEC.optionalFieldOf("target_actions", List.of()).forGetter(a -> a.targetActions),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ModifyKnockbackAbility::new));

    public final ApplyTo applyTo;
    public final List<KryptoniteModifiers.ValueModifier> strengthModifiers, ratioXModifiers, ratioZModifiers;
    public final List<Action> targetActions;
    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;
    public final List<DamageCondition> damageConditions;

    public ModifyKnockbackAbility(ApplyTo applyTo, List<KryptoniteModifiers.ValueModifier> strengthModifiers, List<KryptoniteModifiers.ValueModifier> ratioXModifiers, List<KryptoniteModifiers.ValueModifier> ratioZModifiers, List<Action> targetActions, List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, List<DamageCondition> damageConditions, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.applyTo = applyTo;
        this.strengthModifiers = strengthModifiers;
        this.ratioXModifiers = ratioXModifiers;
        this.ratioZModifiers = ratioZModifiers;
        this.targetActions = targetActions;
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
        this.damageConditions = damageConditions;
    }

    public boolean doesApplyToSelf() {
        return applyTo == ApplyTo.SELF;
    }

    public boolean doesApplyToTarget(LivingEntity attacker, LivingEntity target, DamageSource source, float amount) {
        if (this.applyTo != ApplyTo.OTHER) return false;
        if (!damageConditions.isEmpty() && !DamageCondition.checkConditions(damageConditions, source, amount)) return false;
        if (!biEntityConditions.isEmpty() && !BiCondition.checkConditions(biEntityConditions, attacker, target)) return false;
        return true;
    }

    public float applyStrength(float value, LivingEntity owner, AbilityInstance<?> instance) {
        return Math.max(0F, KryptoniteModifiers.applyModifiers(value, strengthModifiers, DataContext.forAbility(owner, instance)));
    }

    public double applyRatioX(double value, LivingEntity owner, AbilityInstance<?> instance) {
        return KryptoniteModifiers.applyModifiers((float) value, ratioXModifiers, DataContext.forAbility(owner, instance));
    }

    public double applyRatioZ(double value, LivingEntity owner, AbilityInstance<?> instance) {
        return KryptoniteModifiers.applyModifiers((float) value, ratioZModifiers, DataContext.forAbility(owner, instance));
    }

    public void runActions(LivingEntity entity) {
        if (!targetActions.isEmpty()) Action.runList(targetActions, DataContext.forEntity(entity));
    }

    public void runBiActions(Entity attacker, LivingEntity target) {
        if (attacker != null && !biEntityActions.isEmpty()) BiAction.runList(biEntityActions, attacker, target);
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
                    .setDescription("Modifies knockback dealt or received. In the context of this ability, the \"actor\" is the entity that deals knockback & the \"target\" is the entity that receives knockback. For the \"apply_to\" field, \"self\" and \"other\" refer to the entity that has this ability.")
                    .addOptional("apply_to", SettingType.enumList(ApplyTo.values()), "Whether this ability modifies knockback received to your \"self\" or knockback dealt to the \"other\" entity.", ApplyTo.OTHER)
                    .add("strength_modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers applied to knockback strength.")
                    .add("ratio_x_modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers applied to knockback X ratio.")
                    .add("ratio_z_modifiers", KryptoniteDocumented.TYPE_VALUE_MODIFIER, "The modifiers applied to knockback Z ratio.")
                    .addOptional("target_actions", TYPE_ACTION_LIST, "If specified, these actions will run on the \"target\" entity upon being knocked back.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "If specified, these actions will be run on either or both the \"actor\" & \"target\" entities. Only used with \"other\".")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only applies when these conditions are fulfilled by the \"actor\" & \"target\" entities. Only used with \"other\".")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, only applies when the damage matches these damage conditions. Only used with \"other\".")
                    .addExampleObject(new ModifyKnockbackAbility(ApplyTo.OTHER, List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(-1), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(), List.of(), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ModifyKnockbackAbility(ApplyTo.OTHER, List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0.75), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(new KryptoniteModifiers.ValueModifier(new StaticValue(0), KryptoniteModifiers.Operation.ADD_BASE_EARLY)), List.of(), List.of(), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum ApplyTo implements StringRepresentable {
        SELF, OTHER;

        public static final Codec<ApplyTo> CODEC = StringRepresentable.fromEnum(ApplyTo::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}