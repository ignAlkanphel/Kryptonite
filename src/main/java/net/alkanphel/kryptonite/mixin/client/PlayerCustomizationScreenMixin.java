package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.client.render.KryptoniteLivingEntityRenderState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.threetag.palladium.client.gui.screen.customization.PlayerCustomizationScreen;
import net.threetag.palladium.customization.CustomizationPreview;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerCustomizationScreen.class)
public class PlayerCustomizationScreenMixin {

    @Inject(method = "renderEntity", at = @At("HEAD"))
    private static void kryptonite$ignoreModelDepth(GuiGraphicsExtractor guiGraphics, int x1, int y1, int x2, int y2, float baseScale, CustomizationPreview transformation, LivingEntityRenderState entityRenderState, CallbackInfo ci) {
        ((KryptoniteLivingEntityRenderState) entityRenderState).kryptonite$setModelDepth(1.0);
    }

}