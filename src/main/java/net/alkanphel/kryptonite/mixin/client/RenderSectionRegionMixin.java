package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.power.ability.ModifyBlockRenderAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RenderSectionRegion.class)
public class RenderSectionRegionMixin {

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void kryptonite$modifyBlockRender(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        for (ModifyBlockRenderAbility ability : ModifyBlockRenderAbility.Cache.get()) {
            if (ability.doesApply(mc.level, pos)) {
                cir.setReturnValue(ability.getBlockState());
                return;
            }
        }
    }

}