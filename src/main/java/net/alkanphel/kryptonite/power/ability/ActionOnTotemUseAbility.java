package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteDocumented;
import net.alkanphel.kryptonite.power.logic.condition.damage.AttackerDamageCondition;
import net.alkanphel.kryptonite.power.logic.condition.damage.internal.DamageCondition;
import net.alkanphel.kryptonite.util.KryptoniteCodecs;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.condition.EntityTypeCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class ActionOnTotemUseAbility extends Ability {

    public static final MapCodec<ActionOnTotemUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            Codec.list(KryptoniteCodecs.HAND_CODEC).xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("hands", EnumSet.allOf(InteractionHand.class)).forGetter(a -> a.hands),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnTotemUseAbility::new));

    public final List<Action> entityActions;
    public final List<DamageCondition> damageConditions;
    public final EnumSet<InteractionHand> hands;

    public ActionOnTotemUseAbility(List<Action> entityActions, List<DamageCondition> damageConditions, EnumSet<InteractionHand> hands, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
        this.damageConditions = damageConditions;
        this.hands = hands;
    }

    public boolean doesApply(DamageSource source, float damageAmount, InteractionHand hand) {
        if (!hands.contains(hand)) return false;
        if (!damageConditions.isEmpty() && !DamageCondition.checkConditions(damageConditions, source, damageAmount)) return false;
        return true;
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_TOTEM_USE.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnTotemUseAbility> {

        @Override
        public MapCodec<ActionOnTotemUseAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnTotemUseAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Totem Use")
                    .setDescription("Runs actions when the entity uses a Totem of Undying to prevent death.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity when the totem activates.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, only runs the actions when these conditions are fulfilled by the damage source that caused the totem to be used.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "Which hand(s) the totem must be in.", EnumSet.allOf(InteractionHand.class))
                    .addExampleObject(new ActionOnTotemUseAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on totem use!"))), List.of(), EnumSet.allOf(InteractionHand.class), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new ActionOnTotemUseAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on Totem Use if death via Iron Golem!"))), List.of(new AttackerDamageCondition(Optional.of(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("iron_golem"))))))))), EnumSet.allOf(InteractionHand.class), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}