package net.alkanphel.kryptonite.mixin.common;

import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.KryptoniteAttachments;
import net.alkanphel.kryptonite.util.AttachmentUtil;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.minecraft.server.level.ServerPlayer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {

    // Keep Inventory ability & attachment
    @Inject(method = "restoreFrom", at = @At(value = "FIELD", opcode = Opcodes.GETFIELD, target = "Lnet/minecraft/server/level/ServerPlayer;enchantmentSeed:I"))
    private void kryptonite$copyInventoryWhenKeeping(ServerPlayer oldPlayer, boolean restoreAll, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;

        if (!AbilityUtil.getEnabledInstances(oldPlayer, KryptoniteAbilitySerializers.KEEP_INVENTORY.get()).isEmpty() || AttachmentUtil.getBoolean(oldPlayer, KryptoniteAttachments.Addon.KEEP_INVENTORY)) {
            player.getInventory().replaceWith(oldPlayer.getInventory());
        }
    }

}