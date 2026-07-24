package net.alkanphel.kryptonite.power.compat.shouldersurfing;

import com.github.exopandora.shouldersurfing.api.client.event.ComputeCameraCouplingEvent;
import com.github.exopandora.shouldersurfing.api.event.IEventBus;
import com.github.exopandora.shouldersurfing.api.plugin.IShoulderSurfingPlugin;
import net.minecraft.client.Minecraft;

public class ShoulderSurfingCompatClient implements IShoulderSurfingPlugin {
    public static void init() {}

    @Override
    public void register(IEventBus eventBus) {
        eventBus.register(this::onComputeCameraCoupling);
    }

    private void onComputeCameraCoupling(ComputeCameraCouplingEvent event) {
        var mc = Minecraft.getInstance();
        if (mc.player != null && ShoulderSurfingCompat.isCameraCoupled(mc.player)) {
            event.setResult(true);
        }
    }

}