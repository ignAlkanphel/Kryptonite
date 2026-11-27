package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class ActionOnLogoutAbility extends Ability {

    public static final MapCodec<ActionOnLogoutAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnLogoutAbility::new));

    public final List<Action> action;

    public ActionOnLogoutAbility(List<Action> action, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
    }

    public static void runActions(ServerPlayer player) {
        for (AbilityInstance<ActionOnLogoutAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ACTION_ON_LOGOUT.get())) {
            Action.runList(instance.getAbility().action, DataContext.forEntity(player));
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_LOGOUT.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnLogoutAbility> {

        @Override
        public MapCodec<ActionOnLogoutAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnLogoutAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the player logs out.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed after logging out.")
                    .addExampleObject(new ActionOnLogoutAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}