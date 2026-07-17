package net.alkanphel.kryptonite.mixin.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.ModifyBlockDestroySpeedAbility;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourMixin {

    @ModifyExpressionValue(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getDestroySpeed(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float kryptonite$modifyBlockDestroySpeedHardness(float original, BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float value = original;

        for (AbilityInstance<ModifyBlockDestroySpeedAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.MODIFY_BLOCK_DESTROY_SPEED.get())) {
            var ability = instance.getAbility();
            if (!ability.doesApply(player, pos)) continue;

            value = ability.applyHardnessModifiers(value, instance, player);
        }

        return value;
    }

    @ModifyReturnValue(method = "getDestroyProgress", at = @At("RETURN"))
    private float kryptonite$modifyBlockDestroySpeed(float original, BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float value = original;

        for (AbilityInstance<ModifyBlockDestroySpeedAbility> instance : AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.MODIFY_BLOCK_DESTROY_SPEED.get())) {
            var ability = instance.getAbility();
            if (!ability.doesApply(player, pos)) continue;

            value = ability.applySpeedModifiers(value, instance, player);
        }

        return value;
    }

}