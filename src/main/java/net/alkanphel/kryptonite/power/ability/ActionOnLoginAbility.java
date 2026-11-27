package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ActionOnLoginAbility extends Ability {

    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    public static final MapCodec<ActionOnLoginAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnLoginAbility::new));

    public final List<Action> action;

    public ActionOnLoginAbility(List<Action> action, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
    }

    public static void flag(ServerPlayer player) {
        PENDING.put(player.getUUID(), player.tickCount + 1);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity instanceof ServerPlayer player) {
            Integer loginTick = PENDING.remove(player.getUUID());

            if (loginTick != null && player.tickCount <= loginTick) {
                for (AbilityInstance<ActionOnLoginAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ACTION_ON_LOGIN.get())) {
                    Action.runList(instance.getAbility().action, DataContext.forEntity(player));
                }
            }
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_LOGIN.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnLoginAbility> {

        @Override
        public MapCodec<ActionOnLoginAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnLoginAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the player logs in.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed after logging in.")
                    .addExampleObject(new ActionOnLoginAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}