package net.alkanphel.kryptonite.network.p2s;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.util.apoli.keybind.KeyBindActivityManager;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record C2SKeyBindActivity(Set<String> pressedKeys) implements CustomPacketPayload {

    public static final Type<C2SKeyBindActivity> TYPE = new Type<>(Kryptonite.id("c2s/keybind_activity"));

    public static final StreamCodec<RegistryFriendlyByteBuf, C2SKeyBindActivity> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.collection(HashSet::new, ByteBufCodecs.STRING_UTF8), C2SKeyBindActivity::pressedKeys,
            C2SKeyBindActivity::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(C2SKeyBindActivity packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }

            KeyBindActivityManager.getOrCreate(player).update(packet.pressedKeys());
        });
    }

}