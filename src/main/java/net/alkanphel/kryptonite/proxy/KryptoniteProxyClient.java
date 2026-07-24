package net.alkanphel.kryptonite.proxy;

import net.alkanphel.kryptonite.Kryptonite;
import net.alkanphel.kryptonite.mixin.client.MultiPlayerGameModeAccessor;
import net.alkanphel.kryptonite.network.p2c.*;
import net.alkanphel.kryptonite.power.ability.ModifyBlockRenderAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public class KryptoniteProxyClient extends KryptoniteProxy {

    @Override
    public void displayItemActivation(ItemStack stack) {
        var mc = Minecraft.getInstance();
        if (mc.player == null) return;

        mc.gameRenderer.displayItemActivation(stack);
    }

    @Override
    public void modifyBlockRender(ModifyBlockRenderAbility.Mode mode) {
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        switch (mode) {
            case REFRESH_ALL -> Minecraft.getInstance().levelRenderer.allChanged();
            case REFRESH_VISIBLE -> {
                int renderDistance = mc.options.getEffectiveRenderDistance();
                int playerSectionX = SectionPos.blockToSectionCoord(mc.player.blockPosition().getX());
                int playerSectionZ = SectionPos.blockToSectionCoord(mc.player.blockPosition().getZ());

                for (int x = -renderDistance; x <= renderDistance; x++) {
                    for (int z = -renderDistance; z <= renderDistance; z++) {
                        for (int y = mc.level.getMinSectionY(); y <= mc.level.getMaxSectionY(); y++) {
                            mc.levelRenderer.setSectionDirty(playerSectionX + x, y, playerSectionZ + z);
                        }
                    }
                }
            }
        }
    }

    @Override
    public BlockPos getBreakingBlockPos(Player player) {
        var mc = Minecraft.getInstance();
        if (mc.gameMode == null) return null;
        MultiPlayerGameModeAccessor accessor = (MultiPlayerGameModeAccessor) mc.gameMode;
        return accessor.kryptonite$isDestroying() ? accessor.kryptonite$getDestroyBlockPos() : null;
    }

    @Override
    public void packetHandleS2CSyncAttacker(S2CSyncAttacker packet, IPayloadContext context) {
        var level = Minecraft.getInstance().level;
        if (level == null) return;

        Entity target = context.player().level().getEntity(packet.targetId());
        if (!(target instanceof LivingEntity livingTarget)) {
            Kryptonite.LOGGER.warn("Received packet for syncing the attacker of {} entity!", (target == null ? "an unknown" : "a non-living"));
            return;
        }

        Optional<Integer> attackerId = packet.attackerId();
        if (attackerId.isEmpty()) {
            livingTarget.setLastHurtByMob(null);
            return;
        }

        Entity attacker = context.player().level().getEntity(attackerId.get());
        if (!(attacker instanceof LivingEntity livingAttacker)) {
            Kryptonite.LOGGER.warn("Received packet for syncing non-living attacker of entity \"{}\"!", target.getName().getString());
            return;
        }

        livingTarget.setLastHurtByMob(livingAttacker);
    }

}