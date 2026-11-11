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
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

public class ProjectileImpactAbility extends Ability {

    public static final MapCodec<ProjectileImpactAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("shooter_actions", List.of()).forGetter(a -> a.shooterActions),
            Condition.CODEC.optionalFieldOf("shooter_conditions", TrueCondition.INSTANCE).forGetter(a -> a.shooterConditions),
            Action.LIST_CODEC.optionalFieldOf("projectile_actions", List.of()).forGetter(a -> a.projectileActions),
            Condition.CODEC.optionalFieldOf("projectile_conditions", TrueCondition.INSTANCE).forGetter(a -> a.projectileConditions),
            ImpactResult.CODEC.optionalFieldOf("impact_result", ImpactResult.DEFAULT).forGetter(a -> a.impactResult),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ProjectileImpactAbility::new));

    public final List<Action> shooterActions;
    public final Condition shooterConditions;
    public final List<Action> projectileActions;
    public final Condition projectileConditions;
    public final ImpactResult impactResult;

    public ProjectileImpactAbility(List<Action> shooterActions, Condition shooterConditions, List<Action> projectileActions, Condition projectileConditions, ImpactResult impactResult, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.shooterActions = shooterActions;
        this.shooterConditions = shooterConditions;
        this.projectileActions = projectileActions;
        this.projectileConditions = projectileConditions;
        this.impactResult = impactResult;
    }

    public boolean doesApply(LivingEntity holder, Projectile projectile, AbilityInstance<?> abilityInstance) {
        Entity shooter = projectile.getOwner();
        if (shooter == null) return false;

        if (!shooterConditions.test(DataContext.forAffected(shooter, holder, abilityInstance))) {
            return false;
        }

        return projectileConditions.test(DataContext.forAffected(projectile, holder, abilityInstance));
    }

    public void runActions(LivingEntity holder, Projectile projectile, AbilityInstance<?> abilityInstance) {
        Entity shooter = projectile.getOwner();

        if (shooter != null) {
            Action.runList(shooterActions, DataContext.forAffected(shooter, holder, abilityInstance));
        }

        Action.runList(projectileActions, DataContext.forAffected(projectile, holder, abilityInstance));
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
                    .setDescription("Performs actions when a projectile impacts the entity. For this ability, the \"source\" is the entity being hit & the \"target\" is the projectile or its shooter.")
                    .addOptional("shooter_actions", TYPE_ACTION_LIST, "Action(s) that are performed on the projectile's shooter.")
                    .addOptional("shooter_conditions", TYPE_CONDITION_LIST, "Condition(s) the projectile's shooter must meet for the ability to run.")
                    .addOptional("projectile_actions", TYPE_ACTION_LIST, "Action(s) that are performed on the projectile.")
                    .addOptional("projectile_conditions", TYPE_CONDITION_LIST, "Condition(s) the projectile must meet for the ability to run.")
                    .addOptional("impact_result", SettingType.enumList(ImpactResult.values()), "How the projectile is processed upon impact. \"default\" is vanilla, \"ignore\" will make the projectile phase through you, & \"discard\" will destroy the projectile upon hitting you.", ImpactResult.DEFAULT)
                    .addExampleObject(new ProjectileImpactAbility(List.of(new RunCommandAction(new ParsedCommands(List.of("say Shooter actions!")))), TrueCondition.INSTANCE, List.of(new RunCommandAction(new ParsedCommands(List.of("say Projectile actions!")))), new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("arrow")))))), ImpactResult.IGNORE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

    public enum ImpactResult implements StringRepresentable {
        DEFAULT, IGNORE, DISCARD;

        public static final Codec<ImpactResult> CODEC = StringRepresentable.fromEnum(ImpactResult::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

}