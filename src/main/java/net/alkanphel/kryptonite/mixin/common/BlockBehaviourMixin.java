package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.alkanphel.kryptonite.power.ability.ModifyBlockDestroySpeedAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @ModifyExpressionValue(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroySpeed(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float kryptonite$modifyBlockDestroySpeedHardness(float original, BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return ModifyBlockDestroySpeedAbility.applyHardnessModifiers(original, player, pos);
    }

    @ModifyReturnValue(method = "getDestroyProgress", at = @At("RETURN"))
    private float kryptonite$modifyBlockDestroySpeed(float original, BlockState state, Player player, BlockGetter level, BlockPos pos) {
        return ModifyBlockDestroySpeedAbility.applySpeedModifiers(original, player, pos);
    }

}