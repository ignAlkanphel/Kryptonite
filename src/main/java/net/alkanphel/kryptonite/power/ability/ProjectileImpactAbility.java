package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.action.bi.internal.BiAction;
import net.alkanphel.kryptonite.power.logic.action.bi.meta.TargetActionBiAction;
import net.alkanphel.kryptonite.power.logic.condition.bi.internal.BiCondition;
import net.alkanphel.kryptonite.power.logic.condition.bi.meta.TargetConditionBiCondition;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ProjectileImpactAbility extends Ability {

    public static final MapCodec<ProjectileImpactAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            BiAction.LIST_CODEC.optionalFieldOf("bientity_actions", List.of()).forGetter(a -> a.biEntityActions),
            BiCondition.LIST_CODEC.optionalFieldOf("bientity_conditions", List.of()).forGetter(a -> a.biEntityConditions),
            BiAction.LIST_CODEC.optionalFieldOf("projectile_actions", List.of()).forGetter(a -> a.projectileActions),
            BiCondition.LIST_CODEC.optionalFieldOf("projectile_conditions", List.of()).forGetter(a -> a.projectileConditions),
            ImpactResult.CODEC.optionalFieldOf("impact_result", ImpactResult.DEFAULT).forGetter(a -> a.impactResult),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ProjectileImpactAbility::new));

    public final List<Action> entityActions;
    public final List<BiAction> biEntityActions;
    public final List<BiCondition> biEntityConditions;
    public final List<BiAction> projectileActions;
    public final List<BiCondition> projectileConditions;
    public final ImpactResult impactResult;

    public ProjectileImpactAbility(List<Action> entityActions, List<BiAction> biEntityActions, List<BiCondition> biEntityConditions, List<BiAction> projectileActions, List<BiCondition> projectileConditions, ImpactResult impactResult, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.biEntityActions = biEntityActions;
        this.biEntityConditions = biEntityConditions;
        this.projectileActions = projectileActions;
        this.projectileConditions = projectileConditions;
        this.impactResult = impactResult;
    }

    public boolean doesApply(LivingEntity holder, Projectile projectile) {
        Entity shooter = projectile.getOwner();

        if (!projectileConditions.isEmpty() && !BiCondition.checkConditions(projectileConditions, holder, projectile)) {
            return false;
        }

        if (!biEntityConditions.isEmpty()) {
            if (shooter == null) return false;

            if (!BiCondition.checkConditions(biEntityConditions, holder, shooter)) {
                return false;
            }
        }

        return true;
    }

    public void runActions(LivingEntity holder, Projectile projectile) {
        Entity shooter = projectile.getOwner();

        if (!entityActions.isEmpty()) {
            Action.runList(entityActions, DataContext.forEntity(holder));
        }

        if (!biEntityActions.isEmpty() && shooter != null) {
            BiAction.runList(biEntityActions,  holder, shooter);
        }

        if (!projectileActions.isEmpty()) {
            BiAction.runList(projectileActions, holder, projectile);
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PROJECTILE_IMPACT.get();
    }

    public static class Serializer extends AbilitySerializer<ProjectileImpactAbility> {

        @Override
        public MapCodec<ProjectileImpactAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ProjectileImpactAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Projectile Impact")
                    .setDescription("Runs actions when a projectile impacts the entity. In the context of this ability, the \"actor\" is the ability holder & \"target\" the projectile/shooter.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity being hit.")
                    .addOptional("bientity_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on the entity being hit and the projectile's shooter.")
                    .addOptional("bientity_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only enables when these bi conditions are fulfilled.")
                    .addOptional("projectile_actions", KryptoniteDocumented.TYPE_BI_ACTION_LIST, "The bi actions to run on the projectile itself.")
                    .addOptional("projectile_conditions", KryptoniteDocumented.TYPE_BI_CONDITION_LIST, "If specified, only enables when these bi conditions are fulfilled by the projectile itself.")
                    .addOptional("impact_result", SettingType.enumList(ImpactResult.values()), "How the projectile is processed upon impact. \"default\" is vanilla, \"ignore\" will make the projectile phase through you, & \"discard\" will destroy the projectile upon hitting you.", ImpactResult.DEFAULT)
                    .addExampleObject(new ProjectileImpactAbility(List.of(new RunCommandAction(new ParsedCommands(Collections.singletonList("say Entity Actions!")))), List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands(List.of("say Bi-Entity Actions!")))))), List.of(), List.of(new TargetActionBiAction(List.of(new RunCommandAction(new ParsedCommands(List.of("say Projectile Actions!")))))), List.of(new TargetConditionBiCondition(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("arrow")))))))), ImpactResult.IGNORE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum ImpactResult implements StringRepresentable {
        DEFAULT, // vanilla
        IGNORE, // cancel (pass-through)
        DISCARD; // cancel + discard

        public static final Codec<ImpactResult> CODEC = StringRepresentable.fromEnum(ImpactResult::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}