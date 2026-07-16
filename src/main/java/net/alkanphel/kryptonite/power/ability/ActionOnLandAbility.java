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
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.ParsedCommands;

import java.util.List;

public class ActionOnLandAbility extends Ability {

    public static final MapCodec<ActionOnLandAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("entity_actions", List.of()).forGetter(a -> a.entityActions),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnLandAbility::new));

    public final List<Action> entityActions;

    public ActionOnLandAbility(List<Action> entityActions, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.entityActions = entityActions;
    }

    public void runActions(LivingEntity entity) {
        if (!entityActions.isEmpty()) Action.runList(entityActions, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_LAND.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnLandAbility> {

        @Override
        public MapCodec<ActionOnLandAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnLandAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Land")
                    .setDescription("Run actions when the entity lands after being airborne.")
                    .addOptional("entity_actions", TYPE_ACTION_LIST, "The actions to run on the entity upon it landing.")
                    .addExampleObject(new ActionOnLandAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on land!"))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}