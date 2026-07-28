package net.alkanphel.kryptonite.network.p2c;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.ModifyEntityTypeTagAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public record S2CSyncEntityTypeTagCache(Map<Identifier, Collection<Identifier>> subTags) implements CustomPacketPayload {

    public static final Type<S2CSyncEntityTypeTagCache> TYPE = new Type<>(Identifier.fromNamespaceAndPath(Kryptonite.MOD_ID, "s2c/sync_entity_type_tag_cache"));

    public static final StreamCodec<RegistryFriendlyByteBuf, S2CSyncEntityTypeTagCache> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.map(HashMap::new, Identifier.STREAM_CODEC, ByteBufCodecs.collection(ArrayList::new, Identifier.STREAM_CODEC)),
            S2CSyncEntityTypeTagCache::subTags,
            S2CSyncEntityTypeTagCache::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(S2CSyncEntityTypeTagCache packet, IPayloadContext context) {
        context.enqueueWork(() -> ModifyEntityTypeTagAbility.receiveTagCache(packet));
    }

}