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

import java.util.*;

public class ActionOnRespawnAbility extends Ability {

    private static final Map<UUID, Integer> PENDING = new HashMap<>();

    public static final MapCodec<ActionOnRespawnAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("actions", List.of()).forGetter(a -> a.action),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnRespawnAbility::new));

    public final List<Action> action;

    public ActionOnRespawnAbility(List<Action> action, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.action = action;
    }

    public static void flag(ServerPlayer player) {
        PENDING.put(player.getUUID(), player.tickCount + 1);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity instanceof ServerPlayer player) {
            Integer respawnTick = PENDING.remove(player.getUUID());

            if (respawnTick != null && player.tickCount <= respawnTick) {
                for (AbilityInstance<ActionOnRespawnAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.ACTION_ON_RESPAWN.get())) {
                    Action.runList(instance.getAbility().action, DataContext.forEntity(player));
                }
            }
        }
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_RESPAWN.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnRespawnAbility> {

        @Override
        public MapCodec<ActionOnRespawnAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnRespawnAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Performs actions when the player respawns.")
                    .addOptional("actions", TYPE_ACTION_LIST, "Action(s) performed after respawning.")
                    .addExampleObject(new ActionOnRespawnAbility(List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}