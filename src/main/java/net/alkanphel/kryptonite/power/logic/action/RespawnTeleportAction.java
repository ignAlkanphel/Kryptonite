package net.alkanphel.kryptonite.power.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.alkanphel.kryptonite.power.KryptoniteActionSerializers;
import net.alkanphel.kryptonite.util.apoli.MiscUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.action.ActionSerializer;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.StaticValue;
import net.threetag.palladium.logic.value.Value;
import org.jetbrains.annotations.NotNull;

public class RespawnTeleportAction extends Action {

    public static final MapCodec<RespawnTeleportAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Mode.CODEC.optionalFieldOf("mode", Mode.LOCAL).forGetter(ab -> ab.mode),
            Value.CODEC.optionalFieldOf("use_charge", new StaticValue(false)).forGetter(ab -> ab.useCharge),
            Value.CODEC.optionalFieldOf("keep_rotation", new StaticValue(true)).forGetter(ab -> ab.keepRotation)
    ).apply(instance, RespawnTeleportAction::new));

    public final Mode mode;
    public final Value useCharge, keepRotation;

    public RespawnTeleportAction(Mode mode, Value useCharge, Value keepRotation) {
        this.mode = mode;
        this.useCharge = useCharge;
        this.keepRotation = keepRotation;
    }

    @Override
    public boolean run(DataContext context) {
        if (!(context.getEntity() instanceof ServerPlayer player)) return false;

        return switch (this.mode) {
            case LOCAL -> MiscUtil.teleportToLocalVanillaRespawnPosition(player, this.useCharge.getAsBoolean(context), this.keepRotation.getAsBoolean(context));
            case GLOBAL -> MiscUtil.teleportToGlobalVanillaRespawnPosition(player, true);
        };
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return KryptoniteActionSerializers.RESPAWN_TELEPORT.get();
    }

    public static class Serializer extends ActionSerializer<RespawnTeleportAction> {

        @Override
        public MapCodec<RespawnTeleportAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, RespawnTeleportAction> builder, HolderLookup.Provider provider) {
            builder.setName("Respawn Teleport")
                    .setDescription("Teleports the player to either their \"local\" spawn (e.g. bed/respawn anchor), or \"global\" spawn (where you spawn if no local spawn exists).")
                    .addOptional("mode", SettingType.enumList(Mode.values()), "The respawn mode to use.", Mode.LOCAL)
                    .addOptional("use_charge", TYPE_VALUE, "If to consume a charge on the player's respawn anchor if applicable.", false)
                    .addOptional("keep_rotation", TYPE_VALUE, "If true, the teleport will NOT reset the player's rotation.", true)
                    .addExampleObject(new RespawnTeleportAction(Mode.LOCAL, new StaticValue(false), new StaticValue(true)));

        }
    }

    public enum Mode implements StringRepresentable {
        LOCAL, GLOBAL;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public @NotNull String getSerializedName() {
            return name().toLowerCase();
        }
    }

}