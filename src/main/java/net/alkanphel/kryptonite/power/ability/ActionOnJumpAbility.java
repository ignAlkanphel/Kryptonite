package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.RunCommandAction;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.Ability;
import net.threetag.palladium.power.ability.AbilityProperties;
import net.threetag.palladium.power.ability.AbilitySerializer;
import net.threetag.palladium.power.ability.AbilityStateManager;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnJumpAbility extends Ability {

    public static final MapCodec<ActionOnJumpAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.fieldOf("entity_actions").forGetter(a -> a.entityActions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnJumpAbility::new));

    public final List<Action> entityActions;

    public ActionOnJumpAbility(List<Action> entityActions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_JUMP.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnJumpAbility> {

        @Override
        public MapCodec<ActionOnJumpAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnJumpAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Jump")
                    .setDescription("Runs actions when the entity jumps.")
                    .add("entity_actions", TYPE_ACTION_LIST, "Actions to run on the entity upon it jumping.")
                    .addExampleObject(new ActionOnJumpAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on jump!"))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}