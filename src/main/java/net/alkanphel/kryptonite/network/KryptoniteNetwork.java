package net.alkanphel.kryptonite.network;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.network.p2c.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = Kryptonite.MOD_ID)
public class KryptoniteNetwork {
    public static void init() {}

    @SubscribeEvent
    static void register(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        // Client -> Server

        // Server -> Client
        registrar.playToClient(S2CSyncAttacker.TYPE, S2CSyncAttacker.STREAM_CODEC, S2CSyncAttacker::handle);
    }

}