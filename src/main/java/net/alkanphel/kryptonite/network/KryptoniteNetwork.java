package net.alkanphel.kryptonite.network;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.network.p2c.*;
import net.alkanphel.kryptonite.network.p2s.*;
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
        registrar.playToServer(C2SKeyBindActivity.TYPE, C2SKeyBindActivity.STREAM_CODEC, C2SKeyBindActivity::handle);

        // Server -> Client
        registrar.playToClient(S2CSyncAttacker.TYPE, S2CSyncAttacker.STREAM_CODEC, S2CSyncAttacker::handle);
        registrar.playToClient(S2CDisplayItemActivation.TYPE, S2CDisplayItemActivation.STREAM_CODEC, S2CDisplayItemActivation::handle);
    }

}