package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.util.apoli.access.BlockBreakDirectionHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerGameMode.class)
public class ServerPlayerGameModeMixin implements BlockBreakDirectionHolder {

    @Unique private Direction kryptonite$direction;

    @Override
    public void kryptonite$setDirection(Direction direction) {
        this.kryptonite$direction = direction;
    }

    @Override
    public Direction kryptonite$getDirection() {
        return this.kryptonite$direction;
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"))
    private void captureDirection(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence, CallbackInfo ci) {
        this.kryptonite$direction = direction;
    }

}