package net.alkanphel.kryptonite.mixin.client;

import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MultiPlayerGameMode.class)
public interface MultiPlayerGameModeAccessor {
    @Accessor("destroyBlockPos")
    BlockPos kryptonite$getDestroyBlockPos();

    @Accessor("isDestroying")
    boolean kryptonite$isDestroying();
}