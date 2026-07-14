package net.alkanphel.kryptonite.network.p2c;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record S2CSyncAttacker(int targetId, Optional<Integer> attackerId) implements CustomPacketPayload {

    public static final Type<S2CSyncAttacker> TYPE = new Type<>(Kryptonite.id("s2c/sync_attacker"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncAttacker> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, S2CSyncAttacker::targetId,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), S2CSyncAttacker::attackerId,
            S2CSyncAttacker::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CSyncAttacker packet, IPayloadContext context) {
        context.enqueueWork(() -> Kryptonite.PROXY.packetHandleS2CSyncAttacker(packet, context));
    }

}