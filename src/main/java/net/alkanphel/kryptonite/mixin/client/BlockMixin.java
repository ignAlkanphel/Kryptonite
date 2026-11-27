package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Block.class)
public abstract class BlockMixin extends BlockBehaviour implements ItemLike {

    public BlockMixin(Properties properties) {
        super(properties);
    }

    // Prevent Slowdown (slime_block) ability
    @ModifyReturnValue(method = "getFriction", at = @At("RETURN"))
    public float kryptonite$preventSlowdownSlimeBlockFriction(float original) {
        if ((Object) this != Blocks.SLIME_BLOCK) return original;

        var player = Minecraft.getInstance().player;
        if (player == null) return original;

        return PreventSlowdownAbility.preventsBlock(player, PreventSlowdownAbility.ModeBlocks.SLIME_BLOCK) ? 0.6F : original;
    }

}