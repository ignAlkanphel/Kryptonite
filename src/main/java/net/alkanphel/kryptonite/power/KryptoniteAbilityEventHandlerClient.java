package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.threetag.palladium.power.ability.AbilityUtil;

@EventBusSubscriber(modid = Kryptonite.MOD_ID, value = Dist.CLIENT)
public class KryptoniteAbilityEventHandlerClient {

    @SubscribeEvent // Immediate Respawn ability
    public static void onScreenOpening(ScreenEvent.Opening e) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        if (!(e.getNewScreen() instanceof DeathScreen)) return;

        if (AbilityUtil.isTypeEnabled(mc.player, KryptoniteAbilitySerializers.IMMEDIATE_RESPAWN.get())) {
            mc.player.respawn();
            e.setNewScreen(null);
        }
    }

}