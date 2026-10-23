package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.alkanphel.kryptonite.power.attribute.KryptoniteAttributesUtilClient;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GuiEntityRenderer.class)
public class GuiEntityRendererMixin {

    @WrapMethod(method = "renderToTexture(Lnet/minecraft/client/renderer/state/gui/pip/GuiEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;)V")
    private void kryptonite$fGuiRender(GuiEntityRenderState entityState, PoseStack poseStack, Operation<Void> original) {
        boolean previous = KryptoniteAttributesUtilClient.inGui;
        KryptoniteAttributesUtilClient.inGui = true;

        try {
            original.call(entityState, poseStack);
        } finally {
            KryptoniteAttributesUtilClient.inGui = previous;
        }
    }

}