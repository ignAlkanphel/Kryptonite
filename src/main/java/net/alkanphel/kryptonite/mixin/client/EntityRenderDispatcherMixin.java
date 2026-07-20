package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.client.render.OpacityRenderChanging;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    // Prevent Entity Render ability
    @Inject(method = "shouldRender", at = @At("RETURN"), cancellable = true)
    private <E extends Entity> void kryptonite$preventEntityRender(E entity, Frustum culler, double camX, double camY, double camZ, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        var mc = Minecraft.getInstance();
        if (!(mc.player instanceof LivingEntity viewer) || entity == viewer) return;

        if (OpacityRenderChanging.isFullyHidden(viewer, entity, mc.getDeltaTracker().getGameTimeDeltaPartialTick(true))) {
            cir.setReturnValue(false);
        }
    }

}