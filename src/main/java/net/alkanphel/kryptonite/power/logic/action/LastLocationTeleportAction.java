package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.util.player.LastLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.BooleanValueHolder;

import java.util.List;

public class LastLocationTeleportAction extends Action {

    public static final MapCodec<LastLocationTeleportAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BooleanValueHolder.CODEC.optionalFieldOf("async", BooleanValueHolder.TRUE).forGetter(a -> a.async),
            BooleanValueHolder.CODEC.optionalFieldOf("reset_fall_distance", BooleanValueHolder.TRUE).forGetter(a -> a.resetFallDistance),
            BooleanValueHolder.CODEC.optionalFieldOf("keep_rotation", BooleanValueHolder.FALSE).forGetter(a -> a.keepRotation),
            BooleanValueHolder.CODEC.optionalFieldOf("keep_mount", BooleanValueHolder.FALSE).forGetter(a -> a.keepMount),
            Action.LIST_CODEC.optionalFieldOf("action_on_success", List.of()).forGetter(a -> a.onSuccess),
            Action.LIST_CODEC.optionalFieldOf("action_on_fail", List.of()).forGetter(a -> a.onFail)
    ).apply(instance, LastLocationTeleportAction::new));

    private final BooleanValueHolder async, resetFallDistance, keepRotation, keepMount;
    private final List<Action> onSuccess, onFail;

    public LastLocationTeleportAction(BooleanValueHolder async, BooleanValueHolder resetFallDistance, BooleanValueHolder keepRotation, BooleanValueHolder keepMount, List<Action> onSuccess, List<Action> onFail) {
        this.async = async;
        this.resetFallDistance = resetFallDistance;
        this.keepRotation = keepRotation;
        this.keepMount = keepMount;
        this.onSuccess = onSuccess;
        this.onFail = onFail;
    }

    @Override
    protected boolean execute(DataContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        boolean resetFallDistance = this.resetFallDistance.get(context);
        boolean keepRotation = this.keepRotation.get(context);
        boolean keepMount = this.keepMount.get(context);

        if (!this.async.get(context)) {
            boolean success = LastLocation.teleportToLastLocation(player, resetFallDistance, keepRotation, keepMount);
            if (success && !this.onSuccess.isEmpty()) Action.runList(this.onSuccess, context);
            else if (!success && !this.onFail.isEmpty()) Action.runList(this.onFail, context);
            return success;
        }

        Runnable onSuccess = this.onSuccess.isEmpty() ? null : () -> Action.runList(this.onSuccess, context);
        Runnable onFail = this.onFail.isEmpty() ? null : () -> Action.runList(this.onFail, context);

        return LastLocation.teleportToLastLocationAsync(player, resetFallDistance, keepRotation, keepMount, onSuccess, onFail);
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.LAST_LOCATION_TELEPORT.get();
    }

    public static class Serializer extends ActionSerializer<LastLocationTeleportAction> {

        @Override
        public MapCodec<LastLocationTeleportAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, LastLocationTeleportAction> builder, HolderLookup.Provider provider) {
            builder.setName("Last Location Teleport")
                    .setDescription("Teleports the player to their last (saved) location.")
                    .addOptional("async", TYPE_BOOLEAN_VALUE, "If true, pre-loads the last (saved) location chunk before teleportation, unless already loaded.", true)
                    .addOptional("reset_fall_distance", TYPE_BOOLEAN_VALUE, "If true, resets the player's fall distance.", true)
                    .addOptional("keep_rotation", TYPE_BOOLEAN_VALUE, "If true, keeps the player's current rotation.", false)
                    .addOptional("keep_mount", TYPE_BOOLEAN_VALUE, "If true, keeps the player mounted.", false)
                    .addOptional("action_on_success", TYPE_ACTION_LIST, "Action(s) performed once the teleport succeeds.")
                    .addOptional("action_on_fail", TYPE_ACTION_LIST, "Action(s) performed once the teleport fails; no last location, no loaded dimension, or chunk fails to load.", List.of())
                    .addExampleObject(new LastLocationTeleportAction(BooleanValueHolder.TRUE, BooleanValueHolder.TRUE, BooleanValueHolder.FALSE, BooleanValueHolder.TRUE, List.of(), List.of()));
        }
    }

}