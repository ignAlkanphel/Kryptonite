package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.util.player.LastLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class LastLocationModifyAction extends Action {

    public static final MapCodec<LastLocationModifyAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Operation.CODEC.optionalFieldOf("operation", Operation.SAVE).forGetter(a -> a.operation)
    ).apply(instance, LastLocationModifyAction::new));

    private final Operation operation;

    public LastLocationModifyAction(Operation operation) {
        this.operation = operation;
    }

    @Override
    protected boolean execute(DataContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        return switch (this.operation) {
            case SAVE -> LastLocation.saveLastLocation(player);
            case WIPE -> LastLocation.wipeLastLocation(player);
        };
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.LAST_LOCATION_MODIFY.get();
    }

    public static class Serializer extends ActionSerializer<LastLocationModifyAction> {

        @Override
        public MapCodec<LastLocationModifyAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, LastLocationModifyAction> builder, HolderLookup.Provider provider) {
            builder.setName("Last Location Modify")
                    .setDescription("Modifies the player's last (saved) location.")
                    .addOptional("operation", SettingType.enumList(Operation.values()), "Whether to save the player's current location or wipe it.", Operation.SAVE)
                    .addExampleObject(new LastLocationModifyAction(Operation.SAVE))
                    .addExampleObject(new LastLocationModifyAction(Operation.WIPE));
        }
    }

    public enum Operation implements StringRepresentable {
        SAVE, WIPE;

        public static final Codec<Operation> CODEC = StringRepresentable.fromEnum(Operation::values);

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

}