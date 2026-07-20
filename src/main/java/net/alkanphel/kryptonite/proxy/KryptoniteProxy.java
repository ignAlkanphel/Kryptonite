package net.alkanphel.kryptonite.proxy;

import net.alkanphel.kryptonite.network.p2c.S2CSyncAttacker;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class KryptoniteProxy {

    public void displayItemActivation(ItemStack stack) {}

    public BlockPos getBreakingBlockPos(Player player) {
        return null;
    }

    public void packetHandleS2CSyncAttacker(S2CSyncAttacker packet, IPayloadContext context) {}

}