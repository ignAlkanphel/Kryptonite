package net.alkanphel.kryptonite.mixin.client;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SlimeBlock.class)
public abstract class SlimeBlockMixin {

    // Prevent Slowdown (slime_block) ability
    @Inject(method = "stepOn", at = @At("HEAD"), cancellable = true)
    private void kryptonite$preventSlowdownSlimeBlock(Level level, BlockPos pos, BlockState onState, Entity entity, CallbackInfo ci) {
        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null || entity != player) return;

        if (AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get())
                .stream().anyMatch(i -> i.getAbility().modeBlocksPrevents(PreventSlowdownAbility.ModeBlocks.SLIME_BLOCK))) {
            ci.cancel();
        }
    }

}