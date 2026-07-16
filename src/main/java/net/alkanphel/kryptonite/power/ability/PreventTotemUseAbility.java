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
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumHolderSet;
import net.threetag.palladium.util.ParsedCommands;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class PreventTotemUseAbility extends Ability {

    public static final MapCodec<PreventTotemUseAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            DamageCondition.LIST_CODEC.optionalFieldOf("damage_conditions", List.of()).forGetter(a -> a.damageConditions),
            Codec.list(KryptoniteCodecs.HAND_CODEC).xmap(EnumSet::copyOf, List::copyOf).optionalFieldOf("hands", EnumSet.allOf(InteractionHand.class)).forGetter(a -> a.hands),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, PreventTotemUseAbility::new));

    public final List<Action> entityActions;
    public final List<DamageCondition> damageConditions;
    public final EnumSet<InteractionHand> hands;

    public PreventTotemUseAbility(List<Action> entityActions, List<DamageCondition> damageConditions, EnumSet<InteractionHand> hands, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.entityActions = entityActions;
        this.damageConditions = damageConditions;
        this.hands = hands;
    }

    public boolean doesApply(DamageSource source, float amount, InteractionHand hand) {
        return hands.contains(hand) && DamageCondition.checkConditions(damageConditions, source, amount);
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    public static boolean doesPrevent(LivingEntity entity, DamageSource source, float amount, InteractionHand hand) {
        boolean prevented = false;

        for (AbilityInstance<PreventTotemUseAbility> instance : AbilityUtil.getEnabledInstances(entity, KryptoniteAbilitySerializers.PREVENT_TOTEM_USE.get())) {
            if (!instance.getAbility().doesApply(source, amount, hand)) {
                continue;
            }

            instance.getAbility().runActions(entity);
            prevented = true;
        }

        return prevented;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.PREVENT_TOTEM_USE.get();
    }

    public static class Serializer extends AbilitySerializer<PreventTotemUseAbility> {

        @Override
        public MapCodec<PreventTotemUseAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PreventTotemUseAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Prevent Totem Use")
                    .setDescription("Prevents the entity from using a Totem of Undying.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity when totem use is prevented.")
                    .addOptional("damage_conditions", KryptoniteDocumented.TYPE_DAMAGE_CONDITION_LIST, "If specified, totem use is only prevented when these damage conditions are fulfilled.")
                    .addOptional("hands", KryptoniteDocumented.TYPE_INTERACTION_HAND, "Which hand(s) the totem must be in.", EnumSet.allOf(InteractionHand.class))
                    .addExampleObject(new PreventTotemUseAbility(List.of(new RunCommandAction(new ParsedCommands("say Prevented the use of a totem!"))), List.of(), EnumSet.allOf(InteractionHand.class), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()))
                    .addExampleObject(new PreventTotemUseAbility(List.of(new RunCommandAction(new ParsedCommands("say Prevented totem use because you died via by Iron Golem!"))), List.of(new AttackerDamageCondition(Optional.of(new EntityTypeCondition(PalladiumHolderSet.direct(HolderSet.direct(provider.holderOrThrow(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.withDefaultNamespace("iron_golem"))))))))), EnumSet.allOf(InteractionHand.class), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}