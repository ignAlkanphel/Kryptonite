package net.alkanphel.kryptonite.power.ability;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.*;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

// TODO Not sure if to keep this ability in or not
public class ActionOnCallbackAbility extends Ability {

    public static final MapCodec<ActionOnCallbackAbility> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Action.LIST_CODEC.optionalFieldOf("on_spawn", List.of()).forGetter(a -> a.spawn),
            Action.LIST_CODEC.optionalFieldOf("on_load", List.of()).forGetter(a -> a.load),
            Action.LIST_CODEC.optionalFieldOf("on_unload", List.of()).forGetter(a -> a.unload),
            Action.LIST_CODEC.optionalFieldOf("on_gain", List.of()).forGetter(a -> a.gain),
            Action.LIST_CODEC.optionalFieldOf("on_loss", List.of()).forGetter(a -> a.loss),
            propertiesCodec(), stateCodec(), energyBarUsagesCodec()
    ).apply(instance, ActionOnCallbackAbility::new));

    public final List<Action> spawn, load, unload, gain, loss;

    public ActionOnCallbackAbility(List<Action> spawn, List<Action> load, List<Action> unload, List<Action> gain, List<Action> loss, AbilityProperties properties, AbilityStateManager conditions, List<EnergyBarUsage> energyBarUsages) {
        super(properties, conditions, energyBarUsages);
        this.spawn = spawn;
        this.unload = unload;
        this.load = load;
        this.gain = gain;
        this.loss = loss;
    }

    public void onSpawn(LivingEntity entity) {
        if (!spawn.isEmpty()) Action.runList(spawn, DataContext.forEntity(entity));
    }

    public void onUnload(LivingEntity entity) {
        if (!unload.isEmpty()) Action.runList(unload, DataContext.forEntity(entity));
    }

    public void onLoad(LivingEntity entity) {
        if (!load.isEmpty()) Action.runList(load, DataContext.forEntity(entity));
    }

    public void onGain(LivingEntity entity) {
        if (!gain.isEmpty()) Action.runList(gain, DataContext.forEntity(entity));
    }

    public void onLoss(LivingEntity entity) {
        if (!loss.isEmpty()) Action.runList(loss, DataContext.forEntity(entity));
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return KryptoniteAbilitySerializers.ACTION_ON_CALLBACK.get();
    }

    public static class Serializer extends AbilitySerializer<ActionOnCallbackAbility> {

        @Override
        public MapCodec<ActionOnCallbackAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, ActionOnCallbackAbility> builder, HolderLookup.Provider provider) {
            builder.setName("Action On Callback")
                    .setDescription("Runs actions on the entity with this ability & power based on the lifecycle of a power instance.")
                    .addOptional("on_spawn", TYPE_ACTION_LIST, "Runs these actions right after the player respawns & this power is usable.")
                    .addOptional("on_load", TYPE_ACTION_LIST, "Runs these actions when this power is loaded onto the player (e.g. on login & respawn).")
                    .addOptional("on_unload", TYPE_ACTION_LIST, "Runs these actions when this power is unloaded from the player (e.g. on logout & death).")
                    .addOptional("on_gain", TYPE_ACTION_LIST, "Runs these actions when this power is added to the player for the first time.")
                    .addOptional("on_loss", TYPE_ACTION_LIST, "Runs these actions when this power is removed from the player for the last time.")
                    .addExampleObject(new ActionOnCallbackAbility(List.of(), List.of(), List.of(), List.of(), List.of(), AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}