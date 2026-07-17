package net.alkanphel.kryptonite.util.apoli.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.logic.action.item.internal.ItemAction;
import net.alkanphel.kryptonite.power.logic.condition.item.internal.ItemCondition;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class InteractionPrioritizedAbility extends InteractionAbility implements Prioritized {

    public record InteractionPrioritizedFields(InteractionFields interaction, int priority) {
        public static final MapCodec<InteractionPrioritizedFields> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                InteractionFields.CODEC.forGetter(InteractionPrioritizedFields::interaction),
                Codec.INT.optionalFieldOf("priority", 0).forGetter(InteractionPrioritizedFields::priority)
        ).apply(instance, InteractionPrioritizedFields::new));
    }

    private final int priority;

    public InteractionPrioritizedAbility(List<ItemAction> heldItemActions, List<ItemCondition> heldItemConditions, List<ItemAction> resultItemActions, Optional<ItemStack> resultStack, EnumSet<InteractionHand> hands, InteractionResult actionResult, int priority, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(heldItemActions, heldItemConditions, resultItemActions, resultStack, hands, actionResult, properties, conditions, energyBarUsages);
        this.priority = priority;
    }

    @Override
    public int getPriority() {
        return priority;
    }

}