package net.alkanphel.kryptonite.power;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.power.ability.IntangibilityAbility;
import net.alkanphel.kryptonite.power.ability.PreventBlockSelectionAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractBlockOutlineRenderStateEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;

@EventBusSubscriber(modid = Kryptonite.MOD_ID, value = Dist.CLIENT)
public class KryptoniteAbilityEventHandlerClient {

    @SubscribeEvent // Prevent Intangibility ability
    static void renderFog(ViewportEvent.RenderFog e) {
        if (!(e.getCamera().entity() instanceof LivingEntity living)) return;

        for (AbilityInstance<IntangibilityAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.INTANGIBILITY.get())) {
            if (instance.getAbility().renderType == IntangibilityAbility.RenderType.BLINDNESS && IntangibilityAbility.getInWallBlockState(living) != null) {
                e.setFarPlaneDistance(5F);
                e.setNearPlaneDistance(1F);
                return;
            }
        }
    }

    @SubscribeEvent
    static void fogColor(ViewportEvent.ComputeFogColor e) {
        if (!(e.getCamera().entity() instanceof LivingEntity living)) return;

        for (AbilityInstance<IntangibilityAbility> instance : AbilityUtil.getEnabledInstances(living, KryptoniteAbilitySerializers.INTANGIBILITY.get())) {
            if (instance.getAbility().renderType == IntangibilityAbility.RenderType.BLINDNESS && IntangibilityAbility.getInWallBlockState(living) != null) {
                e.setRed(0F);
                e.setGreen(0F);
                e.setBlue(0F);
                return;
            }
        }
    }

    @SubscribeEvent // Prevent Block Selection (Visual Only) ability
    public static void onExtractBlockOutline(ExtractBlockOutlineRenderStateEvent event) {
        if (!(event.getCamera().entity() instanceof Player player)) return;

        if (PreventBlockSelectionAbility.doesPreventOutline(player, event.getBlockPos())) {
            event.setCanceled(true);
        }
    }

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