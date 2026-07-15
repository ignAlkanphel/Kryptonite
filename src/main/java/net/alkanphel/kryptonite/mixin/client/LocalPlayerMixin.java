package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    // Prevent Sprinting ability
    @ModifyReturnValue(method = "canStartSprinting", at = @At("RETURN"))
    private boolean kryptonite$preventSprinting(boolean original) {
        LocalPlayer player = (LocalPlayer) (Object) this;

        if (original && AbilityUtil.isTypeEnabled(player, KryptoniteAbilitySerializers.PREVENT_SPRINTING.get())) {
            return false;
        }

        return original;
    }

}