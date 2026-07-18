package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventSlowdownAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.threetag.palladium.power.ability.AbilityUtil;
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
        Block block = (Block) (Object) this;

        var mc = Minecraft.getInstance();
        var player = mc.player;

        if (player == null) return original;

        if (block == Blocks.SLIME_BLOCK && AbilityUtil.getEnabledInstances(player, KryptoniteAbilitySerializers.PREVENT_SLOWDOWN.get())
                .stream().anyMatch(i -> i.getAbility().modeBlocksPrevents(PreventSlowdownAbility.ModeBlocks.SLIME_BLOCK))) {
            return 0.6F;
        }

        else return original;
    }

}