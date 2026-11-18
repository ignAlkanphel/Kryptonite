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
            Action.LIST_CODEC.fieldOf("actions").forGetter(a -> a.action),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnLandAbility::new));

    public final List<Action> action;

    public ActionOnLandAbility(List<Action> action, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
    }

    public void runActions(LivingEntity entity) {
        if (!action.isEmpty()) Action.runList(action, DataContext.forEntity(entity));
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
                    .setDescription("Performs actions upon landing after being airborne.")
                    .add("actions", TYPE_ACTION_LIST, "Acton(s) performed upon landing.")
                    .addExampleObject(new ActionOnLandAbility(List.of(new RunCommandAction(new ParsedCommands("say Action on land!"))), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}