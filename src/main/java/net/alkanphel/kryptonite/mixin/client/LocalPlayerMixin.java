package net.alkanphel.kryptonite.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import net.alkanphel.kryptonite.power.KryptoniteAbilitySerializers;
import net.alkanphel.kryptonite.power.ability.PreventEntitySelectionAbility;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.power.ability.AbilityUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.util.function.Predicate;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends AbstractClientPlayer {

    public LocalPlayerMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    // Prevent Entity Selection ability
    @ModifyArg(method = "pick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/ProjectileUtil;getEntityHitResult(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;D)Lnet/minecraft/world/phys/EntityHitResult;"), index = 4)
    private static Predicate<Entity> kryptonite$preventEntitySelection(Predicate<Entity> original) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof LivingEntity viewer)) return original;

        return entity -> original.test(entity) && !PreventEntitySelectionAbility.shouldPreventSelection(viewer, entity);
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